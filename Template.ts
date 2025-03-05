import { ColumnDefinition } from './ColumnDefinition';

/**
 * Represents a template for generating test data
 */
export interface Template {
  id?: number;
  name: string;
  description: string;
  columnDefinitions: ColumnDefinition[];
  defaultOutputFormat: OutputFormat;
  defaultRowCount: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

/**
 * Supported output formats for data generation
 */
export enum OutputFormat {
  CSV = 'CSV',
  JSON = 'JSON',
  XML = 'XML'
}

/**
 * Parameters for listing templates
 */
export interface TemplateListParams {
  page?: number;
  size?: number;
  sort?: string;
}

/**
 * Response structure for paginated template listings
 */
export interface TemplateListResponse {
  content: Template[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Parameters for searching templates
 */
export interface TemplateSearchParams {
  name?: string;
  columnType?: string;
}
