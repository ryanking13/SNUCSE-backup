#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "csapp.h"

/* Recommended max cache and object sizes */
#define MAX_CACHE_SIZE 1049000
#define MAX_OBJECT_SIZE 102400
#define MAX_HEADER_SIZE 300
/* You won't lose style points for including this long line in your code */
static const char *user_agent_hdr = "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:10.0.3) Gecko/20120305 Firefox/10.0.3\r\n";
static const char *connection = "Connection: close\r\nProxy-Connection: close\r\n";
char host[100];
char path[100];
char client_port[10];
char server_port[10];

int check_header(char buf[]){
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

int check_protocol(char protocol[]){

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

int main(int argc, char* argv[])
{
	int listen_fd, conn_fd, client_fd;
	struct sockaddr_in clientaddr;
	socklen_t clientlen;
	size_t n;
	rio_t server_rio, client_rio;
	char buf_r[MAX_OBJECT_SIZE];
	char buf_w[MAX_OBJECT_SIZE];
	int header_chk;
	//char host_tp[100];

	strcpy(server_port, argv[1]);
	listen_fd = open_listenfd(server_port);

	while(1){
		clientlen = sizeof(clientaddr);
		conn_fd = Accept(listen_fd, (SA *)&clientaddr, &clientlen);
	
		Rio_readinitb(&server_rio, conn_fd);
		
		// read GET
		Rio_readlineb(&server_rio, buf_r, MAX_OBJECT_SIZE);
		check_protocol(buf_r);
		//strcpy(host_tp, host);
		client_fd = open_clientfd(host, client_port);	
		strcat(buf_w, user_agent_hdr);	
		strcpy(buf_w, "GET ");
		strcat(buf_w, path);
		strcat(buf_w, " HTTP/1.0\r\n");
	
		//rio_writen(client_fd, buf, sizeof(buf));
		while( (n = Rio_readlineb(&server_rio, buf_r, MAX_OBJECT_SIZE)) != 0 ){
			header_chk = check_header(buf_r);
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
		buf_w[0] = '\0';
		while( (n = rio_readlineb(&client_rio, buf_r, MAX_OBJECT_SIZE)) != 0){
			Rio_writen(conn_fd, buf_r, n);
		}
		//Rio_writen(conn_fd, buf_w, sum);

		close(conn_fd);
		close(client_fd);
		printf("%s %s %s\n", host, path, client_port);
	}
		return 0;
}
