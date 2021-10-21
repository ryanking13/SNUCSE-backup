//------------------------------------------------------------------------------
//
// memtrace
//
// trace calls to the dynamic memory manager
//
#define _GNU_SOURCE

#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <memlog.h>
#include <memlist.h>
#include "callinfo.h"

//
// function pointers to stdlib's memory management functions
//
static void *(*mallocp)(size_t size) = NULL;
static void (*freep)(void *ptr) = NULL;
static void *(*callocp)(size_t nmemb, size_t size);
static void *(*reallocp)(void *ptr, size_t size);

//
// statistics & other global variables
//
static unsigned long n_malloc  = 0;
static unsigned long n_calloc  = 0;
static unsigned long n_realloc = 0;
static unsigned long n_allocb  = 0;
static unsigned long n_freeb   = 0;
static item *list = NULL;

//
// init - this function is called once when the shared library is loaded
//
__attribute__((constructor))
void init(void)
{
  char *error;

  LOG_START();

  // initialize a new list to keep track of all memory (de-)allocations
  // (not needed for part 1)
  list = new_list();

  if(!mallocp){
	mallocp = dlsym(RTLD_NEXT, "malloc");
	if((error = dlerror()) != NULL){
		fprintf(stderr, "Error getting symbol 'malloc': %s\n", error);
		exit(1);	
	}
  }
  if(!callocp){
	callocp = dlsym(RTLD_NEXT, "calloc");
	if((error = dlerror()) != NULL){
		fprintf(stderr, "Error getting symbol 'calloc': %s\n", error);
		exit(1);	
	}
  }
  if(!reallocp){
	reallocp = dlsym(RTLD_NEXT, "realloc");
	if((error = dlerror()) != NULL){
		fprintf(stderr, "Error getting symbol 'realloc': %s\n", error);
		exit(1);	
	}
  }
  if(!freep){
	freep = dlsym(RTLD_NEXT, "free");
	if((error = dlerror()) != NULL){
		fprintf(stderr, "Error getting symbol 'free': %s\n", error);
		exit(1);	
	}
  }

}

//
// fini - this function is called once when the shared library is unloaded
//
__attribute__((destructor))
void fini(void)
{
  unsigned long n_total;
  n_total = n_malloc + n_calloc + n_realloc;

  LOG_STATISTICS(n_allocb, n_allocb/n_total, n_freeb);

  item *i = list->next;
  int total_cnt = 0;
  while(i != NULL){
	total_cnt += i->cnt;
	i = i->next;
  }
  if(total_cnt > 0)
	LOG_NONFREED_START();

  i = list->next;
  while(i != NULL){
	if(i->cnt > 0)
  		LOG_BLOCK(i->ptr, i->size, i->cnt, i->fname, i->ofs);
	i = i->next;
  }
  LOG_STOP();

  // free list (not needed for part 1)
  free_list(list);
}

void *malloc(size_t size){

  void *ptr;

  ptr = mallocp(size);
  LOG_MALLOC(size, ptr);
  n_malloc += 1;
  n_allocb += (unsigned long)size;
  alloc(list, ptr, size);

  return ptr;
}

void *calloc(size_t nmemb, size_t size){

  void *ptr;

  ptr = callocp(nmemb, size);
  LOG_CALLOC(nmemb, size, ptr);
  n_calloc += 1;
  n_allocb += (unsigned long)(nmemb * size);
  alloc(list, ptr, nmemb*size);

  return ptr;
}

void *realloc(void *ptr, size_t size){

  void *ptr2;

  ptr2 = reallocp(ptr, size);
  LOG_REALLOC(ptr, size, ptr2);
  n_realloc += 1;
  n_allocb += (unsigned long)(size);
  item *l = find(list, ptr);
  if(l != NULL) n_freeb += (unsigned long)(l->size);
  dealloc(list, ptr);
  alloc(list, ptr2, size);

  return ptr2;
}

void free(void *ptr){

  char *error;
  freep(ptr);
  LOG_FREE(ptr);
  item *l = find(list, ptr);
  if(l != NULL) n_freeb += (unsigned long)(l->size);
  dealloc(list, ptr);
}
