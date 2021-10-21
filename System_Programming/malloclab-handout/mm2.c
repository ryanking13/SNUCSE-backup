// Used segregate free list
// 
// < free block structure >
// __________
// | header	|
// | (prev)	| -> payload space in allocated block
// | (next) | -> payload space in allocated block
// | ...	|
// | footer	|
// ----------
//
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <unistd.h>
#include <string.h>

#include "mm.h"
#include "memlib.h"

/*********************************************************
 * NOTE TO STUDENTS: Before you do anything else, please
 * provide your team information in the following struct.
 ********************************************************/
team_t team = {
    /* Team name */
    "2015-16327",
    /* First member's full name */
    "Choi Gyeongjae",
    /* First member's email address */
    "ryanking13@snu.ac.kr",
    /* Second member's full name (leave blank if none) */
    "",
    /* Second member's email address (leave blank if none) */
    ""
};

/* single word (4) or double word (8) alignment */
#define ALIGNMENT 8
/* rounds up to the nearest multiple of ALIGNMENT */
#define ALIGN(size) (((size) + (ALIGNMENT-1)) & ~0x7)
#define SIZE_T_SIZE (ALIGN(sizeof(size_t)))


#define ALLOCATED 0x1 // allocateed bit (0x0 : not allocated)
#define NUM_LISTS 10 // number of free lists

// ----- Function Prototypes -----
int mm_init(void);
void* mm_malloc(size_t size);
void mm_free(void *ptr);
void* mm_realloc(void* ptr, size_t size);
intptr_t** size_to_list(size_t size);
intptr_t* create_new_node(size_t size);
void remove_node(intptr_t** list, intptr_t* node);
void add_node(intptr_t** list, intptr_t* node);
size_t tag_to_size(int32_t* tag);
// --------------------------------

intptr_t** free_lists;

int mm_check(void){
	
	intptr_t** list;
	intptr_t* node;

	int32_t* header;
	int32_t* footer;
	size_t block_size;

	for(list = free_lists; list < free_lists + NUM_LISTS; list++){
		for(node = *list; node != NULL; node = (intptr_t*)*(node+1)){
			header = (int32_t*)((void*)node-4);
			block_size = tag_to_size(header);
			footer = (int32_t*)((void*)node+block_size-8);
			
			// checks tag consistency
			if(block_size != tag_to_size(footer)){
				printf("TAG SIZE NOT EQUAL\n");
				printf("node: %p\n", node);
				printf("header/footer size: %d %d\n", block_size, tag_to_size(footer));
				return 0;
			}
			
			// check free block not allocated
			if((*header & ALLOCATED)){
				printf("ALLOCATED FREE LIST (header)\n");
				printf("node: %p\n", node);
			    printf("header: %p\n", header);
				return 0;
			}
			// check free block not allocated
			if((*footer & ALLOCATED)){
				printf("ALLOCATED FREE LIST (footer)\n");
				printf("node %p\n", node);
				printf("footer %p\n", footer);
				return 0;
			}

			//printf("tag_to_size: %d\n", tag_to_size(header));
		}
	}

	return 1;
}

int mm_init(void){

	int32_t *boundary; // boundary for coalesing
	int i;

	free_lists = mem_sbrk(sizeof(intptr_t**) * NUM_LISTS);
	for(i = 0; i < NUM_LISTS; i++)
		*(free_lists+i) = NULL;

	// extra boundary for coalesing
	mem_sbrk(8);
	boundary = mem_heap_hi() - 7;
	*boundary = ALLOCATED;
	*(boundary + 1) = ALLOCATED;
    
	return 0;
}

void *mm_malloc(size_t size){

	//printf("malloc start\n");
	intptr_t** list;
	intptr_t* node;
	size_t aligned_size;

	int32_t* header;
	int32_t* footer;
	size_t block_size;

	aligned_size = ALIGN(size);
	//printf("size : %d\n", aligned_size);
	for (list = size_to_list(aligned_size); list < free_lists + NUM_LISTS; list++){
		if(*list == NULL)
			continue;

		for(node = *list; node != NULL; node = (intptr_t*)*(node+1)){
			header = (int32_t*)((void *)node - 4);
			block_size = tag_to_size(header);
			footer = (int32_t*)((void *)node + block_size - 8);

			if(block_size < aligned_size + 8)
				continue;

			//printf("block size %d\n", block_size);
			*header |= ALLOCATED;
			*footer |= ALLOCATED;

			remove_node(list, node);
			mm_check();
			return node;
		}
	}
	
	//printf("malloc create\n");
	mm_check();
	return create_new_node(aligned_size);
}

void mm_free(void *ptr){

	int32_t* header;
	int32_t* footer;
	int32_t* prev_footer;
	int32_t* next_header;
	size_t block_size;
	size_t prev_size;
	size_t next_size;

	//printf("free ptr: %p\n", ptr);
	header = (int32_t*)((void *)ptr - 4);
	block_size = tag_to_size(header);
	footer = (int32_t*)((void *)ptr + block_size - 8);

	prev_footer = header - 1;
	if(!(*(prev_footer) & ALLOCATED)){
		prev_size = tag_to_size(prev_footer);
		block_size += prev_size;
		header = (void *)header - prev_size;
		remove_node(size_to_list(prev_size), (intptr_t*)(header+1));
	}

	next_header = footer + 1;
	if(!(*(next_header) & ALLOCATED)){
		next_size = tag_to_size(next_header);
		block_size += next_size;
		remove_node(size_to_list(next_size), (intptr_t*)(footer+2));
		footer = (void*)footer + next_size;
	}

	*header = block_size;
	*footer = block_size;

	//printf("free: hf : %p %p\n", header, footer);
	
	//printf("create block size\n");
	add_node(size_to_list(block_size), (intptr_t*)(header+1));

	mm_check();
}

void *mm_realloc(void *ptr, size_t size){
	printf("realloc call\n");
    void *oldptr = ptr;
    void *newptr;
    size_t copySize;
    
    newptr = mm_malloc(size);
    if (newptr == NULL)
      return NULL;
    copySize = *(size_t *)((char *)oldptr - SIZE_T_SIZE);
    if (size < copySize)
      copySize = size;
    memcpy(newptr, oldptr, copySize);
    mm_free(oldptr);
    return newptr;
}

void add_node(intptr_t** list, intptr_t* node){
	intptr_t* n;

	//printf("add: node: %p\n", node);
	//printf("add: size: %d\n", *(int32_t*)((void*)node-4));

	if(*list == NULL){
		*list = node;
		*node = (intptr_t)NULL;
		*(node+1) = (intptr_t)NULL;

		return;
	}

	for (n = *list; ; n = (intptr_t*)*(n+1)){
		if(node < n){
			*node = *n;
			*(node+1) = (intptr_t)n;
			*n = (intptr_t)node;

			if(*node == (intptr_t)NULL)
				*list = node;
			else
				*((intptr_t*)*node+1) = (intptr_t)node;
			return;
		}

		if(*(n+1) == (intptr_t)NULL){
			*node = (intptr_t)n;
			*(node+1) = (intptr_t)NULL;
			*(n+1) = (intptr_t)node;
			return;
		}
	}
}

void remove_node(intptr_t** list, intptr_t* node){
	//printf("remove node: %p\n", node);
	if (*node == (intptr_t)NULL)
		*list = (intptr_t*)*(node+1);
	else
		*((intptr_t*)(*node) + 1) = *(node+1);

	if (*(node+1) != (intptr_t)NULL){
		*(intptr_t*)*(node+1) = *node;
	}
}

intptr_t* create_new_node(size_t size){
	size_t node_size;
	intptr_t* node;

	size_t last_block_size;
	int32_t* last_block_footer;

	node_size = size + 8; // including header, footer
	
	last_block_footer = (int32_t*)(mem_heap_hi() - 7);

	if(*last_block_footer & ALLOCATED){
		last_block_size = 0;
		node = (void*)last_block_footer + 8;
	}
	else{
		last_block_size = tag_to_size(last_block_footer);
		node = (void*)last_block_footer - last_block_size + 8;
		remove_node(size_to_list(last_block_size), node);
	}

	mem_sbrk(node_size - last_block_size);

	*(int32_t*)((void*)node - 4) = node_size | ALLOCATED; // header
	*(int32_t*)((void*)node + node_size - 8) = node_size | ALLOCATED; // footer
	
	// boundary update
	*(int32_t*)((void*)node + node_size - 4) = ALLOCATED;
	//printf("new node: %p\n", node);
	//printf("new node size: %d\n", tag_to_size((int32_t*)((void*)node-4)));
	return node;
}

intptr_t** size_to_list(size_t size){
	size_t size_per_list = 1028;
	
	//printf("idx: %d %d\n", size, size/size_per_list);
	if(size/size_per_list >= NUM_LISTS-1)
		return free_lists + NUM_LISTS - 1;

	return free_lists + size/size_per_list;
}

size_t tag_to_size(int32_t* tag){
	return (size_t)( (*tag) & ~0x7);
}

