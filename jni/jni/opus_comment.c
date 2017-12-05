//
//  opus_comment.c
//  OpusTesting
//
//  Created by Kenny Roethel on 4/16/13.
//  Copyright (c) 2013 Mindgrub Technologies. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define readint(buf, base) (((buf[base+3]<<24)&0xff000000)| \
((buf[base+2]<<16)&0xff0000)| \
((buf[base+1]<<8)&0xff00)| \
(buf[base]&0xff))

#define writeint(buf, base, val) do{ buf[base+3]=((val)>>24)&0xff; \
buf[base+2]=((val)>>16)&0xff; \
buf[base+1]=((val)>>8)&0xff; \
buf[base]=(val)&0xff; \
}while(0)

void comment_init(char **comments, int* length, const char *vendor_string)
{
    /*The 'vendor' field should be the actual encoding library used.*/
    int vendor_length=strlen(vendor_string);
    int user_comment_list_length=0;
    int len=8+4+vendor_length+4;
    char *p=(char*)malloc(len);
    if(p==NULL){
        fprintf(stderr, "malloc failed in comment_init()\n");
        exit(1);
    }
    memcpy(p, "OpusTags", 8);
    writeint(p, 8, vendor_length);
    memcpy(p+12, vendor_string, vendor_length);
    writeint(p, 12+vendor_length, user_comment_list_length);
    *length=len;
    *comments=p;
}

void comment_add(char **comments, int* length, char *tag, char *val)
{
    char* p=*comments;
    int vendor_length=readint(p, 8);
    int user_comment_list_length=readint(p, 8+4+vendor_length);
    int tag_len=(tag?strlen(tag):0);
    int val_len=strlen(val);
    int len=(*length)+4+tag_len+val_len;
    
    p=(char*)realloc(p, len);
    if(p==NULL){
        fprintf(stderr, "realloc failed in comment_add()\n");
        exit(1);
    }
    
    writeint(p, *length, tag_len+val_len);      /* length of comment */
    if(tag) memcpy(p+*length+4, tag, tag_len);  /* comment */
    memcpy(p+*length+4+tag_len, val, val_len);  /* comment */
    writeint(p, 8+4+vendor_length, user_comment_list_length+1);
    *comments=p;
    *length=len;
}
