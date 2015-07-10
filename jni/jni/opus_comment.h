//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
//
//  opus_comment.h
//  OpusTesting
//
//  Created by Kenny Roethel on 4/16/13.
//

#ifndef OpusTesting_opus_comment_h
#define OpusTesting_opus_comment_h

void comment_init(char **comments, int* length, const char *vendor_string);
void comment_add(char **comments, int* length, char *tag, char *val);

#endif
