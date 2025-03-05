import { useState, useCallback } from 'react';
import { 
  getTemplates, 
  getTemplateById, 
  createTemplate, 
  updateTemplate, 
  deleteTemplate, 
  searchTemplates,
  Template,
  TemplateListParams,
  TemplateListResponse
} from '../api/templateApi';
import useApi from './useApi';

const useTemplates = () => {
  const [currentTemplate, setCurrentTemplate] = useState<Template | null>(null);
  
  // Get all templates with pagination
  const {
    data: templatesData,
    isLoading: isLoadingTemplates,
    error: templatesError,
    execute: fetchTemplates
  } = useApi<TemplateListResponse, [TemplateListParams?]>(getTemplates);
  
  // Get a template by ID
  const {
    data: templateData,
    isLoading: isLoadingTemplate,
    error: templateError,
    execute: fetchTemplateById
  } = useApi<Template, [number]>(getTemplateById);
  
  // Create a new template
  const {
    data: createData,
    isLoading: isCreating,
    error: createError,
    execute: createTemplateExecute
  } = useApi<Template, [Template]>(createTemplate);
  
  // Update an existing template
  const {
    data: updateData,
    isLoading: isUpdating,
    error: updateError,
    execute: updateTemplateExecute
  } = useApi<Template, [number, Template]>(updateTemplate);
  
  // Delete a template
  const {
    data: deleteData,
    isLoading: isDeleting,
    error: deleteError,
    execute: deleteTemplateExecute
  } = useApi<void, [number]>(deleteTemplate);
  
  // Search templates by name
  const {
    data: searchData,
    isLoading: isSearching,
    error: searchError,
    execute: searchTemplatesExecute
  } = useApi<Template[], [string]>(searchTemplates);
  
  // Helper function to load a template and set as current
  const loadTemplateById = useCallback(async (id: number) => {
    const response = await fetchTemplateById(id);
    if (response.data) {
      setCurrentTemplate(response.data);
    }
    return response;
  }, [fetchTemplateById]);
  
  // Create a template and set as current if successful
  const createNewTemplate = useCallback(async (template: Template) => {
    const response = await createTemplateExecute(template);
    if (response.data) {
      setCurrentTemplate(response.data);
    }
    return response;
  }, [createTemplateExecute]);
  
  // Update a template and update current if it's the same
  const updateExistingTemplate = useCallback(async (id: number, template: Template) => {
    const response = await updateTemplateExecute(id, template);
    if (response.data && currentTemplate && currentTemplate.id === id) {
      setCurrentTemplate(response.data);
    }
    return response;
  }, [updateTemplateExecute, currentTemplate]);
  
  // Delete a template and clear current if it's the same
  const deleteExistingTemplate = useCallback(async (id: number) => {
    const response = await deleteTemplateExecute(id);
    if (!response.error && currentTemplate && currentTemplate.id === id) {
      setCurrentTemplate(null);
    }
    return response;
  }, [deleteTemplateExecute, currentTemplate]);
  
  return {
    // State
    templates: templatesData?.data?.content || [],
    currentTemplate,
    totalCount: templatesData?.data?.totalElements || 0,
    totalPages: templatesData?.data?.totalPages || 0,
    
    // Loading states
    isLoadingTemplates,
    isLoadingTemplate,
    isCreating,
    isUpdating,
    isDeleting,
    isSearching,
    
    // Errors
    templatesError,
    templateError,
    createError,
    updateError,
    deleteError,
    searchError,
    
    // Actions
    fetchTemplates,
    loadTemplateById,
    createTemplate: createNewTemplate,
    updateTemplate: updateExistingTemplate,
    deleteTemplate: deleteExistingTemplate,
    searchTemplates: searchTemplatesExecute,
    setCurrentTemplate
  };
};

export default useTemplates;
