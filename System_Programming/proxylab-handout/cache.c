#include "cache.h"
#include<stdio.h>

size_t total_cache_size = 0;

// initialize cache list head
void init_cache(entry* head){
	total_cache_size = 0;

	strcpy(head->addr, "CACHEHEAD");
	head->size=0;
	head->next=NULL;
}

// free cache list
void free_cache(entry* head){
	entry* curr = NULL;
	entry* next = head;

	while(next != NULL){
		curr = next;
		next = next->next;
		free(curr);
	}
	total_cache_size = 0;
}

// check if object is in cache
entry* check_cache(char addr[], entry* head){
	entry* next = head;

	while (next != NULL){
		if(!strcmp(next->addr, addr)){
			next->time = 0;
			return next;
		}
		else{
			next->time++;
			next= next->next;
		}
	}

	return NULL;
}

// push new entry to cache list
void push_entry(entry* e, entry* head){
	entry* next = head;
	while(next->next != NULL)
		next = next->next;

	next->next = e;
	total_cache_size += e->size;
}

// evict object from cache list
int evict_entry(int size, entry* head){
	entry* curr = NULL;
	entry* next = head;
	int min_time = 0;

	while(next != NULL){
		// LRU and size is enough
		if( next->time > min_time && next->size >= size ){
			curr->next = next->next;
			total_cache_size -= next->size;
			free(next);

			return 1;
		}
		curr = next;
		next = next -> next;
	}

	// fail to evict ( then how ?? )
	return 0;
}

// make new cache list entry
void make_new_entry(entry* e, char addr[], char buf[], int buf_size){	
	strcpy(e->addr, addr);
	memcpy(e->buf, buf, buf_size);
	e->time = 0;
	e->size = buf_size;
	e->next = NULL;
}

int is_size_enough(int size){
	if(total_cache_size + size <= MAX_CACHE_SIZE)
		return 1;
	else
		return 0;
}
