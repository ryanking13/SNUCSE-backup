#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "csapp.h"
#include "cache.h"

/* Recommended max cache and object sizes */
#define MAX_CACHE_SIZE 1049000
#define MAX_OBJECT_SIZE 102400
#define MAX_HEADER_SIZE 300
/* You won't lose style points for including this long line in your code */
static const char *user_agent_hdr = "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:10.0.3) Gecko/20120305 Firefox/10.0.3\r\n";
static const char *connection = "Connection: close\r\nProxy-Connection: close\r\n";
char server_port[10];
entry* head;

int check_header(char buf[], char host[]){
	char *ptr;
	char header_name[30];
	
	ptr = strstr(buf, ":");
	if(ptr == NULL){
		return -1;
	}
	strncpy(header_name, buf, ptr-buf);
	header_name[ptr-buf+1] = '\0';

	if(strncmp(header_name, "User-Agent", 10) == 0){
		return 1;
	}
	else if(strncmp(header_name, "Host", 4) == 0){
		strcpy(host, ptr+2);
		host[strlen(ptr+2)-2] = '\0'; //remove \r\n
		return 2;
	}
	else if(strncmp(header_name, "Connection", 10) == 0 ||
			strncmp(header_name, "Proxy-Connection", 16) == 0){
		return 2;
	}
	else
		return 0;
}

int check_protocol(char protocol[], char host[], char client_port[], char path[]){

	char *ptr;
	ptr = protocol;
	if (strncmp(ptr, "GET", 3) != 0){
		printf("%s\n", ptr);
		printf("NOT PROPER VURB\n");
		return -1;
	}

	ptr = ptr+4;

	if(strncmp(ptr, "http://", 7) != 0){
		printf("%s\n", ptr);
		printf("NOT PROPER PROTOCOL\n");
		return -1;
	}
	
	ptr = ptr+7;
	char *delim_s = strstr(ptr, "/");
	
	strncpy(host, ptr, delim_s - ptr);
	host[delim_s-ptr+1] = '\0';

	//strncpy(path, delim_s, delim_e - delim_s);
	//path[delim_e - delim_s + 1] = '\0';
	strcpy(path, delim_s);
	strtok(path, " ");
	delim_s = strstr(host, ":");
	if(delim_s == NULL){
		strncpy(client_port, "80\0", 3);
	}
	else{
		strcpy(client_port, delim_s+1);
		host[delim_s-host] = '\0';
	}
	return 0;
}

void manage_proxy_request(int conn_fd){

	char host[MAX_HEADER_SIZE];
	char path[MAX_HEADER_SIZE];
	char client_port[10];

	rio_t server_rio, client_rio;
	int client_fd;
	char buf_r[MAX_CACHE_SIZE];
	char buf_w[MAX_CACHE_SIZE];
	char total_path[MAX_HEADER_SIZE];
	int header_chk;
	size_t n;
	entry* cache_entry;

	Rio_readinitb(&server_rio, conn_fd);
		
	// read GET
	Rio_readlineb(&server_rio, buf_r, MAX_HEADER_SIZE);
	strcpy(total_path, buf_r);
	cache_entry = check_cache(total_path, head);

	if(cache_entry != NULL){
		Rio_writen(conn_fd, cache_entry->buf, cache_entry->size);
		return;
	}

	check_protocol(buf_r, host, client_port, path);
	//strcpy(host_tp, host);
	client_fd = open_clientfd(host, client_port);	
	strcpy(buf_w, "GET ");
	strcat(buf_w, path);
	strcat(buf_w, " HTTP/1.0\r\n");
	
	//rio_writen(client_fd, buf, sizeof(buf));
	while( (n = Rio_readlineb(&server_rio, buf_r, MAX_CACHE_SIZE)) != 0 ){
		header_chk = check_header(buf_r, host);
		if( header_chk == 1) // USER-AGENT
			strcat(buf_w, user_agent_hdr);
			//rio_writen(client_fd, user_agent_hdr, sizeof(user_agent_hdr));
		else if( header_chk == 2){ // HOST
			continue;
		}
		else if ( header_chk == -1){ // END OF REQUEST
			break;
		}
		else{
			strcat(buf_w, buf_r);
			//rio_writen(client_fd, buf, n);
		}
	}
	strcat(buf_w, connection);
	strcat(buf_w, "Host: ");
	strcat(buf_w, host);
	strcat(buf_w, "\r\n\r\n");
	
	Rio_readinitb(&client_rio, client_fd);
	Rio_writen(client_fd, buf_w, strlen(buf_w));
	//Rio_readn(client_fd, buf_r, MAX_OBJECT_SIZE);
	//strcpy(buf_w, buf_r);
	
	//Rio_readinitb(&client_rio, client_fd);
	int sum = 0;
	while( (n = rio_readlineb(&client_rio, buf_r, MAX_CACHE_SIZE)) != 0){
		memcpy(buf_w+sum, buf_r, n);
		sum += n;
		//Rio_writen(conn_fd, buf_r, n);
	}
	Rio_writen(conn_fd, buf_w, sum);

	if(sum <= MAX_OBJECT_SIZE){
		entry* e = Malloc(sizeof(entry));
		make_new_entry(e, total_path, buf_w, sum);
		
		if(is_size_enough(sum)){
			push_entry(e, head);
		}
		else{
			if(evict_entry(sum, head))
				push_entry(e, head);
		}
	}
	close(client_fd);
}

void *proxy_thread(void *vargp){
	int conn_fd = *((int *)vargp);
	Pthread_detach(pthread_self());
	Free(vargp);

	manage_proxy_request(conn_fd);
	close(conn_fd);
	return NULL;
}

int main(int argc, char* argv[])
{
	int listen_fd;
	struct sockaddr_in clientaddr;
	socklen_t clientlen;
	pthread_t tid;
	//char host_tp[100];

	head = Malloc(sizeof(entry));
	init_cache(head);
	clientlen = sizeof(clientaddr);
	strcpy(server_port, argv[1]);
	listen_fd = open_listenfd(server_port);

	while(1){
		int *conn_fdp = Malloc(sizeof(int));
		*conn_fdp = Accept(listen_fd, (SA *)&clientaddr, &clientlen);
		Pthread_create(&tid, NULL, proxy_thread, conn_fdp);
	}

	close(listen_fd);
	return 0;
}
