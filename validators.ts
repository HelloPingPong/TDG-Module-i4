/**
 * Utility functions for validating data in the Test Data Generator application
 */

// Basic validators
export const isNullOrUndefined = (value: any): boolean => {
  return value === null || value === undefined;
};

export const isEmptyString = (value: any): boolean => {
  return value === '';
};

export const isNullOrEmpty = (value: any): boolean => {
  return isNullOrUndefined(value) || isEmptyString(value);
};

// String validation
export const validateStringLength = (
  value: string,
  options: { min?: number; max?: number }
): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Value cannot be null or undefined' };
  }
  
  const { min, max } = options;
  
  if (min !== undefined && value.length < min) {
    return { 
      valid: false, 
      message: `Value must be at least ${min} character${min === 1 ? '' : 's'} long` 
    };
  }
  
  if (max !== undefined && value.length > max) {
    return { 
      valid: false, 
      message: `Value cannot exceed ${max} character${max === 1 ? '' : 's'}` 
    };
  }
  
  return { valid: true };
};

export const validateRegexPattern = (
  value: string,
  pattern: RegExp,
  errorMessage: string = 'Value does not match the required pattern'
): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Value cannot be null or undefined' };
  }
  
  if (!pattern.test(value)) {
    return { valid: false, message: errorMessage };
  }
  
  return { valid: true };
};

// Number validation
export const validateNumberRange = (
  value: number,
  options: { min?: number; max?: number }
): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Value cannot be null or undefined' };
  }
  
  if (isNaN(value)) {
    return { valid: false, message: 'Value must be a number' };
  }
  
  const { min, max } = options;
  
  if (min !== undefined && value < min) {
    return { valid: false, message: `Value must be at least ${min}` };
  }
  
  if (max !== undefined && value > max) {
    return { valid: false, message: `Value cannot exceed ${max}` };
  }
  
  return { valid: true };
};

export const validateInteger = (value: number): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Value cannot be null or undefined' };
  }
  
  if (isNaN(value)) {
    return { valid: false, message: 'Value must be a number' };
  }
  
  if (!Number.isInteger(value)) {
    return { valid: false, message: 'Value must be an integer' };
  }
  
  return { valid: true };
};

export const validatePositiveNumber = (value: number): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Value cannot be null or undefined' };
  }
  
  if (isNaN(value)) {
    return { valid: false, message: 'Value must be a number' };
  }
  
  if (value <= 0) {
    return { valid: false, message: 'Value must be positive' };
  }
  
  return { valid: true };
};

// Date validation
export const validateDateRange = (
  value: Date,
  options: { min?: Date; max?: Date }
): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Date cannot be null or undefined' };
  }
  
  if (!(value instanceof Date) || isNaN(value.getTime())) {
    return { valid: false, message: 'Invalid date' };
  }
  
  const { min, max } = options;
  
  if (min !== undefined && value < min) {
    return { 
      valid: false, 
      message: `Date must be on or after ${min.toLocaleDateString()}` 
    };
  }
  
  if (max !== undefined && value > max) {
    return { 
      valid: false, 
      message: `Date must be on or before ${max.toLocaleDateString()}` 
    };
  }
  
  return { valid: true };
};

export const validateDateString = (
  value: string,
  format: 'ISO' | 'MM/DD/YYYY' | 'DD/MM/YYYY' = 'ISO'
): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Date string cannot be null or undefined' };
  }
  
  let isValid = false;
  let date: Date | null = null;
  
  if (format === 'ISO') {
    // ISO format: YYYY-MM-DD
    isValid = /^\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}:\d{2}(\.\d{3})?Z?)?$/.test(value);
    if (isValid) {
      date = new Date(value);
      isValid = !isNaN(date.getTime());
    }
  } else if (format === 'MM/DD/YYYY') {
    // US format: MM/DD/YYYY
    isValid = /^(0[1-9]|1[0-2])\/(0[1-9]|[12][0-9]|3[01])\/\d{4}$/.test(value);
    if (isValid) {
      const parts = value.split('/');
      date = new Date(Number(parts[2]), Number(parts[0]) - 1, Number(parts[1]));
      isValid = !isNaN(date.getTime());
    }
  } else if (format === 'DD/MM/YYYY') {
    // UK format: DD/MM/YYYY
    isValid = /^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/\d{4}$/.test(value);
    if (isValid) {
      const parts = value.split('/');
      date = new Date(Number(parts[2]), Number(parts[1]) - 1, Number(parts[0]));
      isValid = !isNaN(date.getTime());
    }
  }
  
  if (!isValid) {
    return { valid: false, message: 'Invalid date format' };
  }
  
  return { valid: true };
};

// Common format validators
export const validateEmail = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Email cannot be null or undefined' };
  }
  
  // Basic email regex
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  
  if (!emailRegex.test(value)) {
    return { valid: false, message: 'Invalid email address' };
  }
  
  return { valid: true };
};

export const validatePhoneNumber = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Phone number cannot be null or undefined' };
  }
  
  // Basic US phone number regex, can be adjusted for international numbers
  const phoneRegex = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;
  
  if (!phoneRegex.test(value)) {
    return { valid: false, message: 'Invalid phone number' };
  }
  
  return { valid: true };
};

export const validateZipCode = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Zip code cannot be null or undefined' };
  }
  
  // US zip code regex (5 digits or 5+4)
  const zipRegex = /^\d{5}(-\d{4})?$/;
  
  if (!zipRegex.test(value)) {
    return { valid: false, message: 'Invalid zip code' };
  }
  
  return { valid: true };
};

export const validateUrl = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'URL cannot be null or undefined' };
  }
  
  try {
    new URL(value);
    return { valid: true };
  } catch (e) {
    return { valid: false, message: 'Invalid URL' };
  }
};

// Application-specific validators
export const validateTemplateName = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Template name cannot be null or undefined' };
  }
  
  if (value.trim() === '') {
    return { valid: false, message: 'Template name cannot be empty' };
  }
  
  if (value.length < 3) {
    return { valid: false, message: 'Template name must be at least 3 characters long' };
  }
  
  if (value.length > 50) {
    return { valid: false, message: 'Template name cannot exceed 50 characters' };
  }
  
  return { valid: true };
};

export const validateColumnName = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Column name cannot be null or undefined' };
  }
  
  if (value.trim() === '') {
    return { valid: false, message: 'Column name cannot be empty' };
  }
  
  // Column names should be alphanumeric with underscores, no spaces
  const columnRegex = /^[a-zA-Z][a-zA-Z0-9_]*$/;
  
  if (!columnRegex.test(value)) {
    return { 
      valid: false, 
      message: 'Column name must start with a letter and contain only letters, numbers, and underscores' 
    };
  }
  
  return { valid: true };
};

export const validateCronExpression = (value: string): { valid: boolean; message?: string } => {
  if (isNullOrUndefined(value)) {
    return { valid: false, message: 'Cron expression cannot be null or undefined' };
  }
  
  if (value.trim() === '') {
    return { valid: false, message: 'Cron expression cannot be empty' };
  }
  
  // Basic cron syntax validation
  // Format: second minute hour day-of-month month day-of-week
  const parts = value.trim().split(/\s+/);
  
  if (parts.length < 5 || parts.length > 6) {
    return { 
      valid: false, 
      message: 'Cron expression must have 5 to 6 parts (seconds optional)' 
    };
  }
  
  // This is a simplified validation, a more thorough one would check ranges for each part
  const cronPartRegex = /^(\*|[0-9,-\/]+)$/;
  
  for (const part of parts) {
    if (!cronPartRegex.test(part)) {
      return { valid: false, message: 'Invalid cron expression format' };
    }
  }
  
  return { valid: true };
};

export const validateTemplateHasColumns = (
  columnDefinitions: any[]
): { valid: boolean; message?: string } => {
  if (!Array.isArray(columnDefinitions)) {
    return { valid: false, message: 'Column definitions must be an array' };
  }
  
  if (columnDefinitions.length === 0) {
    return { valid: false, message: 'Template must have at least one column' };
  }
  
  return { valid: true };
};

export const validateTemplateConstraints = (
  columnType: string,
  constraints: Record<string, any>
): { valid: boolean; message?: string } => {
  // Specific validation logic based on column type
  switch (columnType) {
    case 'string':
      if (constraints.minLength !== undefined && constraints.maxLength !== undefined &&
          constraints.minLength > constraints.maxLength) {
        return { 
          valid: false, 
          message: 'Minimum length cannot be greater than maximum length' 
        };
      }
      break;
    
    case 'number':
      if (constraints.min !== undefined && constraints.max !== undefined &&
          constraints.min > constraints.max) {
        return { 
          valid: false, 
          message: 'Minimum value cannot be greater than maximum value' 
        };
      }
      break;
    
    case 'date':
      if (constraints.minDate && constraints.maxDate) {
        const minDate = new Date(constraints.minDate);
        const maxDate = new Date(constraints.maxDate);
        
        if (!isNaN(minDate.getTime()) && !isNaN(maxDate.getTime()) && minDate > maxDate) {
          return { 
            valid: false, 
            message: 'Minimum date cannot be later than maximum date' 
          };
        }
      }
      break;
  }
  
  return { valid: true };
};

// Validation helper for form validation
export const validateForm = (
  formData: Record<string, any>,
  validationRules: Record<string, (value: any) => { valid: boolean; message?: string }>
): Record<string, string> => {
  const errors: Record<string, string> = {};
  
  Object.entries(validationRules).forEach(([field, validateFn]) => {
    const result = validateFn(formData[field]);
    if (!result.valid && result.message) {
      errors[field] = result.message;
    }
  });
  
  return errors;
};
