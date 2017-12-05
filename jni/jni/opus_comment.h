//
//  opus_comment.h
//  OpusTesting
//
//  Created by Kenny Roethel on 4/16/13.
//  Copyright (c) 2013 Mindgrub Technologies. All rights reserved.
//

#ifndef OpusTesting_opus_comment_h
#define OpusTesting_opus_comment_h

void comment_init(char **comments, int* length, const char *vendor_string);
void comment_add(char **comments, int* length, char *tag, char *val);

#endif
