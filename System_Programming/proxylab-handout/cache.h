#ifndef __CACHE_H__
#define __CACHE_H__

#include "csapp.h"

#define MAX_CACHE_SIZE 1049000
#define MAX_OBJECT_SIZE 102400
#define MAX_HEADER_SIZE 300

typedef struct entry{
	char addr[MAX_HEADER_SIZE];
	char buf[MAX_OBJECT_SIZE];
	int time;
	int size;
	struct entry* next;
} entry;

void init_cache(entry* head);
void free_cache(entry* head);

entry* check_cache(char addr[], entry* head);
void push_entry(entry* e, entry* head);
int evict_entry(int size, entry* head);
void make_new_entry(entry* e, char addr[], char buf[], int buf_size);
int is_size_enough(int new_size);

#endif
