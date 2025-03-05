import { useCallback } from 'react';
import { getDataTypes, DataType } from '../api/templateApi';
import useApi from './useApi';

const useDataTypes = (options = { immediate: false }) => {
  // Get all data types and their metadata
  const {
    data: dataTypesData,
    isLoading,
    error,
    execute: fetchDataTypes
  } = useApi<Record<string, DataType>, []>(getDataTypes, [], { 
    immediate: options.immediate 
  });
  
  // Format data types for select component
  const getDataTypeOptions = useCallback(() => {
    if (!dataTypesData?.data) return [];
    
    // Group by category
    const categorizedTypes: Record<string, { label: string, value: string }[]> = {};
    
    Object.entries(dataTypesData.data).forEach(([type, metadata]) => {
      const category = metadata.category || 'Other';
      
      if (!categorizedTypes[category]) {
        categorizedTypes[category] = [];
      }
      
      categorizedTypes[category].push({
        label: metadata.displayName,
        value: type
      });
    });
    
    // Convert to array of option groups
    return Object.entries(categorizedTypes).map(([category, options]) => ({
      label: category,
      options: options.sort((a, b) => a.label.localeCompare(b.label))
    }));
  }, [dataTypesData]);
  
  // Get metadata for a specific data type
  const getDataTypeMetadata = useCallback((type: string) => {
    if (!dataTypesData?.data) return null;
    return dataTypesData.data[type] || null;
  }, [dataTypesData]);
  
  // Get constraint metadata for a data type
  const getConstraintMetadata = useCallback((type: string) => {
    const metadata = getDataTypeMetadata(type);
    return metadata?.constraintsMetadata || {};
  }, [getDataTypeMetadata]);
  
  return {
    // State
    dataTypes: dataTypesData?.data || {},
    dataTypeOptions: getDataTypeOptions(),
    
    // Loading state
    isLoading,
    
    // Error
    error,
    
    // Actions
    fetchDataTypes,
    getDataTypeMetadata,
    getConstraintMetadata
  };
};

export default useDataTypes;
