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

/* macro functions */
#define ALIGN(size) (((size) + (ALIGNMENT-1)) & ~0x7)
#define SIZE_T_SIZE (ALIGN(sizeof(size_t)))

#define GET_HEADER(node) ((int *)((void *)(node) - 4))
#define GET_FOOTER(node, node_size) ((int *)((void*)(node) + node_size - 8))
#define GET_SIZE(tag) ((size_t)( (*tag) & ~0x7))

/* constants */
#define ALLOCATED 0x1 // allocateed bit (0x0 : not allocated)
#define NUM_LISTS 40 // number of free lists
#define ALIGNMENT 8

/* Function Prototypes */
int mm_init(void);
void* mm_malloc(size_t size);
void mm_free(void *ptr);
void* mm_realloc(void* ptr, size_t size);
int** size_to_list(size_t size);
int* allocate_new_node(size_t size);
void remove_node(int** list, int* node);
void add_node(int** list, int* node);


// segregated free list
int** free_lists;

// heap consistency checker
int mm_check(void){
	
	int** list;
	int* node;

	int* header;
	int* footer;
	size_t block_size;

	for(list = free_lists; list < free_lists + NUM_LISTS; list++){
		for(node = *list; node != NULL; node = (int*)*(node+1)){
			header = GET_HEADER(node);
			block_size = GET_SIZE(header);
			footer = GET_FOOTER(node, block_size);
			
			// checks tag consistency
			if(block_size != GET_SIZE(footer)){
				printf("TAG SIZE NOT EQUAL\n");
				printf("node: %p\n", node);
				printf("header/footer size: %d %d\n", block_size, GET_SIZE(footer));
				return -1;
			}
			
			// check free block not allocated
			if((*header & ALLOCATED)){
				printf("ALLOCATED FREE LIST (header)\n");
				printf("node: %p\n", node);
			    printf("header: %p\n", header);
				return -1;
			}
			// check free block not allocated
			if((*footer & ALLOCATED)){
				printf("ALLOCATED FREE LIST (footer)\n");
				printf("node %p\n", node);
				printf("footer %p\n", footer);
				return -1;
			}

		}
	}

	return 1;
}


int mm_init(void){

	int* boundary; // boundary for coalesing
	int i;

	// initial size free lists
	free_lists = mem_sbrk(sizeof(int**) * NUM_LISTS);
	for(i = 0; i < NUM_LISTS; i++)
		*(free_lists+i) = NULL;

	// extra boundary for coalesing
	//mem_sbrk(((0xb - (size_t)(mem_heap_hi() + 4)) & 0x7) + 8);
	mem_sbrk(8);
	boundary = mem_heap_hi() - 7;
	*boundary = ALLOCATED;
	*(boundary + 1) = ALLOCATED;
    
	return 0;
}

void *mm_malloc(size_t size){

	// printf("malloc start\n"); // FOR DEBUG
	
	int** list;
	int* node;
	int* header;
	int* footer;

	size_t aligned_size; // 8-byte aligned size
	size_t block_size; // total block size
	
	aligned_size = ALIGN(size);

	for (list = size_to_list(aligned_size); list < free_lists + NUM_LISTS; list++){

		// *(node+1) : next node address
		for(node = *list; node != NULL; node = (int*)*(node+1)){
			header = GET_HEADER(node);
			block_size = GET_SIZE(header);
			if(block_size < aligned_size + 8) // if no enough block size
				continue;
			footer = GET_FOOTER(node, block_size);

			// printf("block size %d\n", block_size); // FOR DEBUG
			
			// set tag ALLOCATED
			*header |= ALLOCATED;
			*footer |= ALLOCATED;

			// remove node from free list
			remove_node(list, node);
			
			return node;
		}
	}

	// if no space in free list, allocate new space
	node = allocate_new_node(aligned_size);
	return node;
}

void mm_free(void *ptr){

	int* header;
	int* footer;
	int* prev_footer;
	int* next_header;
	size_t block_size;
	size_t prev_size;
	size_t next_size;

	// printf("free ptr: %p\n", ptr); // FOR DEBUG
	
	header = GET_HEADER(ptr);
	block_size = GET_SIZE(header);
	footer = GET_FOOTER(ptr, block_size);

	// if prev block is free, do coalesing
	prev_footer = header - 1;
	if(!(*(prev_footer) & ALLOCATED)){
		prev_size = GET_SIZE(prev_footer);
		block_size += prev_size;
		header = (void *)header - prev_size; // prev block's header position
		remove_node(size_to_list(prev_size), (int*)(header+1)); // remove prev block from free list
	}

	// if next block is free, do coalesing
	next_header = footer + 1;
	if(!(*(next_header) & ALLOCATED)){
		next_size = GET_SIZE(next_header);
		block_size += next_size;
		remove_node(size_to_list(next_size), (int*)(footer+2));
		footer = (void*)footer + next_size; // next block's footer position
	}

	//printf("free: hf : %p %p\n", header, footer); // FOR DEBUG
	
	// set new block size to tags
	*header = block_size;
	*footer = block_size;

	// add new node to free list
	add_node(size_to_list(block_size), (int*)(header+1));

	//mm_check();
}

void *mm_realloc(void *ptr, size_t size){
	
	// printf("realloc call\n");
    
    void *newptr;
    size_t copy_size;
    
    newptr = mm_malloc(size);
	copy_size = GET_SIZE(GET_HEADER(ptr));
	if (size < copy_size)
      copy_size = size;

    memcpy(newptr, ptr, copy_size);
    mm_free(ptr);

	// mm_check();
    return newptr;
}

/* add node to list */
void add_node(int** list, int* node){
	
	// printf("add_node: node, size %p %d\n", node, GET_SIZE(GET_HEADER(node))); // FOR DEBUG

	int *n;

	// if list is empty, attach it first
	if(*list == NULL){
		*list = node;
		*node = (int)NULL; // prev
		*(node+1) = (int)NULL; // next
		return;
	}

	// push it address sorted order
	for (n = *list; ; n = (int*)*(n+1)){
		
		// found appropriate position
		if(node < n){
			// insert node
			*node = *n;
			*(node+1) = (int)n;
			*n = (int)node;

			// if first node
			if(*node == (int)NULL)
				*list = node;
			else
				*((int*)*node+1) = (int)node; // prev_node.next_node = node

			return;
		}
		
		// if not found appropriate position : attach it last
		if(*(n+1) == (int)NULL){
			*node = (int)n;
			*(node+1) = (int)NULL;
			*(n+1) = (int)node;
			return;
		}
	}
}

/* remove node from list */
void remove_node(int** list, int* node){

	// printf("remove node: %p\n", node); // FOR DEBUG
	
	// if node is first node in list
	if (*node == (int)NULL)
		*list = (int*)*(node+1); // change second node to first node
	else
		*((int*)(*node) + 1) = *(node+1); // prev_node.next_node = node.next_node

	// if node is last node in list
	if (*(node+1) == (int)NULL)
		return;
	else
		*(int*)*(node+1) = *node; // node.next_node = prev_node.next_node
}

/* allocate new heap space */
int* allocate_new_node(size_t size){

	size_t block_size;
	int* node;
	int* header;
	int* footer;

	size_t last_block_size;
	int* last_block_footer;

	block_size = size + 8; // size including header, footer
	
	last_block_footer = (int*)(mem_heap_hi() - 7); // [footer-boundary]

	// if last block is allocated block
	if(*last_block_footer & ALLOCATED){
		last_block_size = 0;
		node = (void*)last_block_footer + 8;
	}
	else{ // if last block is free block
		last_block_size = GET_SIZE(last_block_footer);
		node = (void*)last_block_footer - last_block_size + 8;
		remove_node(size_to_list(last_block_size), node);
	}

	mem_sbrk(block_size - last_block_size);
	header = GET_HEADER(node);
	footer = GET_FOOTER(node, block_size);

	*header = block_size | ALLOCATED;
	*footer = block_size | ALLOCATED;
	
	// boundary update
	*(int*)((void*)node + block_size - 4) = ALLOCATED;

	//printf("new node: node, size %p %d\n", node, block_size); // FOR DEBUG
	return node;
}

int** size_to_list(size_t size){
	size_t size_per_list = 16;
	
	//printf("idx: %d %d\n", size, size/size_per_list);
	if(size/size_per_list >= NUM_LISTS-1)
		return free_lists + NUM_LISTS-1;

	return free_lists + size/size_per_list;
}

