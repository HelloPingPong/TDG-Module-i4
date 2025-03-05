import { OutputFormat } from './Template';

/**
 * Represents a scheduled data generation job
 */
export interface Schedule {
  id?: number;
  name: string;
  description?: string;
  templateId: number;
  templateName?: string;
  status?: ScheduleStatus;
  scheduleType?: ScheduleType;
  nextRunTime?: string;
  cronExpression?: string;
  rowCount: number;
  outputFormat: OutputFormat;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  lastRunTime?: string;
  lastRunResult?: string;
}

/**
 * Status of a generation schedule
 */
export enum ScheduleStatus {
  CREATED = 'CREATED',
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  ERROR = 'ERROR'
}

/**
 * Type of schedule
 */
export enum ScheduleType {
  ONE_TIME = 'ONE_TIME',
  RECURRING = 'RECURRING'
}

/**
 * Parameters for listing schedules
 */
export interface ScheduleListParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: string;
  search?: string;
}

/**
 * Response structure for paginated schedule listings
 */
export interface ScheduleListResponse {
  content: Schedule[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Information about schedule execution progress
 */
export interface ExecutionHistory {
  id: number;
  scheduleId: number;
  startTime: string;
  endTime?: string;
  status: 'RUNNING' | 'COMPLETED' | 'FAILED';
  result?: string;
  rowsGenerated?: number;
  executionTimeMs?: number;
}

/**
 * Common cron expression preset
 */
export interface CronPreset {
  label: string;
  value: string;
  description?: string;
}
