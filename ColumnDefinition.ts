/**
 * Defines a column within a template
 */
export interface ColumnDefinition {
  id?: number;
  name: string;
  type: string;
  sequenceNumber: number;
  isNullable: boolean;
  nullProbability: number;
  constraints: Record<string, any>;
}

/**
 * Metadata about a data type
 */
export interface DataTypeMetadata {
  type: string;
  displayName: string;
  category: string;
  description: string;
  constraintsMetadata: Record<string, string>;
}

/**
 * Grouped options for data type selection
 */
export interface DataTypeOptionGroup {
  label: string;
  options: {
    value: string;
    label: string;
  }[];
}

/**
 * Definition for a constraint on a column
 */
export interface ConstraintDefinition {
  name: string;
  label: string;
  type: 'string' | 'number' | 'boolean' | 'date' | 'select' | 'text';
  defaultValue: any;
  options?: { value: string; label: string }[];
  description?: string;
}
