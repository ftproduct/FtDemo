/**
 * Validation utilities using Zod
 */

import { z } from 'zod';

/**
 * Common validation schemas
 */
export const paginationSchema = z.object({
  page: z.coerce.number().int().positive().default(1),
  page_size: z.coerce.number().int().positive().max(100).default(10),
});

export const idSchema = z.string().min(1);

/**
 * Validate and parse query parameters
 */
export function validateQueryParams<T extends z.ZodTypeAny>(
  schema: T,
  params: unknown
): z.infer<T> {
  return schema.parse(params);
}

/**
 * Safe parse with error handling
 */
export function safeParse<T extends z.ZodTypeAny>(
  schema: T,
  data: unknown
): { success: true; data: z.infer<T> } | { success: false; error: z.ZodError } {
  const result = schema.safeParse(data);
  if (result.success) {
    return { success: true, data: result.data };
  }
  return { success: false, error: result.error };
}
