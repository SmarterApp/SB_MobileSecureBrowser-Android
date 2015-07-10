//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
/*
 * dirac.h
 */

#ifndef _DIRAC_H
#define _DIRAC_H

#include <ogg/ogg.h>

typedef struct {
  ogg_uint32_t major_version;
  ogg_uint32_t minor_version;
  ogg_uint32_t profile;
  ogg_uint32_t level;
  ogg_uint32_t chroma_format;
  ogg_uint32_t video_format;

  ogg_uint32_t width;
  ogg_uint32_t height;
  ogg_uint32_t fps_numerator;
  ogg_uint32_t fps_denominator;

  ogg_uint32_t interlaced;
  ogg_uint32_t top_field_first;
} dirac_info;

/**
 * \return -1 Error: parse failure, invalid size index
 * \return 0 Success
 */
extern int dirac_parse_info (dirac_info *info, unsigned char *data, long len);

#endif
