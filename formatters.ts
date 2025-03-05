/**
 * Utility functions for formatting data in the Test Data Generator application
 */

// Date formatting
export const formatDate = (date: Date | string | number, options?: Intl.DateTimeFormatOptions): string => {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' || typeof date === 'number'
    ? new Date(date)
    : date;
  
  if (isNaN(dateObj.getTime())) {
    return '';
  }
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  };
  
  return new Intl.DateTimeFormat('en-US', options || defaultOptions).format(dateObj);
};

export const formatDateTime = (date: Date | string | number): string => {
  return formatDate(date, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const formatTime = (date: Date | string | number): string => {
  return formatDate(date, {
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const formatRelativeTime = (date: Date | string | number): string => {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' || typeof date === 'number'
    ? new Date(date)
    : date;
  
  if (isNaN(dateObj.getTime())) {
    return '';
  }
  
  const now = new Date();
  const diffMs = dateObj.getTime() - now.getTime();
  const diffSec = Math.round(diffMs / 1000);
  const diffMin = Math.round(diffSec / 60);
  const diffHour = Math.round(diffMin / 60);
  const diffDay = Math.round(diffHour / 24);
  
  // Past
  if (diffMs < 0) {
    if (diffSec > -60) return 'Just now';
    if (diffMin > -60) return `${Math.abs(diffMin)} minutes ago`;
    if (diffHour > -24) return `${Math.abs(diffHour)} hours ago`;
    if (diffDay > -7) return `${Math.abs(diffDay)} days ago`;
    return formatDate(date);
  }
  
  // Future
  if (diffSec < 60) return 'Just now';
  if (diffMin < 60) return `In ${diffMin} minutes`;
  if (diffHour < 24) return `In ${diffHour} hours`;
  if (diffDay < 7) return `In ${diffDay} days`;
  return formatDate(date);
};

// Number formatting
export const formatNumber = (value: number, options?: Intl.NumberFormatOptions): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '';
  }
  
  const defaultOptions: Intl.NumberFormatOptions = {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  };
  
  return new Intl.NumberFormat('en-US', options || defaultOptions).format(value);
};

export const formatCurrency = (value: number, currency: string = 'USD'): string => {
  return formatNumber(value, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};

export const formatPercentage = (value: number, decimals: number = 2): string => {
  return formatNumber(value, {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });
};

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
};

// String formatting
export const truncateText = (text: string, maxLength: number, ellipsis: string = '...'): string => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  
  return text.substring(0, maxLength) + ellipsis;
};

export const capitalizeFirstLetter = (text: string): string => {
  if (!text) return '';
  return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
};

export const toTitleCase = (text: string): string => {
  if (!text) return '';
  return text
    .toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const kebabToCamelCase = (text: string): string => {
  if (!text) return '';
  return text.replace(/-([a-z])/g, (g) => g[1].toUpperCase());
};

export const camelToKebabCase = (text: string): string => {
  if (!text) return '';
  return text.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
};

export const sanitizeFileName = (fileName: string): string => {
  if (!fileName) return '';
  return fileName
    .replace(/[\\/:*?"<>|]/g, '_') // Replace invalid file characters
    .replace(/\s+/g, '_');         // Replace spaces with underscores
};

// Application-specific formatters
export const formatCronExpression = (cron: string): string => {
  if (!cron) return '';
  
  // Map for common cron expressions
  const cronMap: Record<string, string> = {
    '0 0 * * *': 'Daily at midnight',
    '0 0 * * 1-5': 'Weekdays at midnight',
    '0 9 * * 1-5': 'Weekdays at 9 AM',
    '0 12 * * *': 'Daily at noon',
    '0 0 * * 0': 'Weekly on Sunday at midnight',
    '0 0 * * 1': 'Weekly on Monday at midnight',
    '0 0 1 * *': 'Monthly on the 1st at midnight',
    '0 0 15 * *': 'Monthly on the 15th at midnight',
  };
  
  return cronMap[cron] || cron;
};

// Format output format for display
export const formatOutputType = (type: 'CSV' | 'JSON' | 'XML'): string => {
  return type; // Currently just returns the type, can be extended for more complex formatting
};

// Format template type for display
export const formatDataType = (type: string, displayMap?: Record<string, string>): string => {
  if (!type) return '';
  
  const defaultDisplayMap: Record<string, string> = {
    'firstName': 'First Name',
    'lastName': 'Last Name',
    'fullName': 'Full Name',
    'email': 'Email',
    'phoneNumber': 'Phone Number',
    'ssn': 'SSN',
    'streetAddress': 'Street Address',
    'city': 'City',
    'state': 'State',
    'zipCode': 'Zip Code',
    'country': 'Country',
    'date': 'Date',
    'time': 'Time',
    'dateTime': 'Date & Time',
    'accountNumber': 'Account Number',
    'currency': 'Currency',
    'creditCardNumber': 'Credit Card Number',
    'idNumber': 'ID Number',
    'uuid': 'UUID',
    'string': 'String',
    'number': 'Number',
    'boolean': 'Boolean',
  };
  
  const map = displayMap || defaultDisplayMap;
  return map[type] || toTitleCase(type.replace(/([A-Z])/g, ' $1').trim());
};

// Format schedule status for display
export const formatScheduleStatus = (status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ERROR' | 'CREATED'): string => {
  const statusMap: Record<string, string> = {
    'ACTIVE': 'Active',
    'PAUSED': 'Paused',
    'COMPLETED': 'Completed',
    'ERROR': 'Error',
    'CREATED': 'Created'
  };
  
  return statusMap[status] || status;
};

// Get status color for schedule status
export const getStatusColor = (status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ERROR' | 'CREATED'): string => {
  const colorMap: Record<string, string> = {
    'ACTIVE': 'var(--jpm-accent)',
    'PAUSED': 'var(--jpm-warning)',
    'COMPLETED': 'var(--jpm-info)',
    'ERROR': 'var(--jpm-error)',
    'CREATED': 'var(--jpm-neutral-500)'
  };
  
  return colorMap[status] || 'var(--jpm-neutral-500)';
};
