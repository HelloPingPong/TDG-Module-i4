import { OutputFormat } from './Template';

/**
 * Request for batch generation of data from multiple templates
 */
export interface BatchRequest {
  templateIds: number[];
  rowCount?: number;
  outputFormat?: OutputFormat;
  parallel?: boolean;
  filename?: string;
}

/**
 * Result of a batch generation operation
 */
export interface BatchResult {
  batchId: string;
  results: BatchItemResult[];
  status: BatchStatus;
  startTime: string;
  endTime?: string;
  totalDurationMs?: number;
  downloadUrl?: string;
}

/**
 * Result for an individual template in a batch operation
 */
export interface BatchItemResult {
  templateId: number;
  templateName?: string;
  success: boolean;
  message: string;
  durationMs: number;
  outputFormat?: OutputFormat;
  dataSize?: number;
  dataPreview?: string;
  downloadUrl?: string;
  status: BatchItemStatus;
}

/**
 * Status of a batch generation job
 */
export enum BatchStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

/**
 * Status of an individual batch item
 */
export enum BatchItemStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED'
}

/**
 * Options for batch generation mode
 */
export interface BatchExecutionOptions {
  mode: 'SEQUENTIAL' | 'PARALLEL';
  maxParallelJobs?: number;
  continueOnError?: boolean;
}
