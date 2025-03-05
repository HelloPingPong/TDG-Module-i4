import React, { useState, useEffect } from 'react';
import { 
  Database, 
  FileText, 
  Layers, 
  RefreshCw, 
  CheckCircle, 
  AlertTriangle 
} from 'react-feather';
import Card from '../../common/Card';
import Button from '../../common/Button';
import Input from '../../common/Input';
import Select from '../../common/Select';
import Alert from '../../common/Alert';
import { Template, OutputFormat } from '../../../models/Template';
import { BatchRequest } from '../../../models/BatchRequest';
import useTemplates from '../../../hooks/useTemplates';
import { generateBatch } from '../../../api/batchApi';

interface BatchGenerationFormProps {
  onGenerationComplete?: (results: any) => void;
}

const BatchGenerationForm: React.FC<BatchGenerationFormProps> = ({ 
  onGenerationComplete 
}) => {
  const { templates, isLoadingTemplates, templatesError, fetchTemplates } = useTemplates();
  
  // State for selected templates
  const [selectedTemplateIds, setSelectedTemplateIds] = useState<number[]>([]);
  
  // Form state
  const [formValues, setFormValues] = useState({
    rowCount: 100,
    outputFormat: OutputFormat.CSV,
    parallel: false
  });
  
  // Processing state
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Load templates on mount
  useEffect(() => {
    fetchTemplates();
  }, [fetchTemplates]);
  
  // Handle toggling template selection
  const toggleTemplateSelection = (templateId: number) => {
    setSelectedTemplateIds(prev => {
      if (prev.includes(templateId)) {
        return prev.filter(id => id !== templateId);
      } else {
        return [...prev, templateId];
      }
    });
  };
  
  // Handle select/deselect all
  const handleSelectAll = () => {
    if (selectedTemplateIds.length === templates.length) {
      setSelectedTemplateIds([]);
    } else {
      setSelectedTemplateIds(templates.map(t => t.id as number));
    }
  };
  
  // Handle input changes
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type, checked } = e.target as HTMLInputElement;
    
    if (type === 'checkbox') {
      setFormValues({
        ...formValues,
        [name]: checked
      });
    } else if (name === 'rowCount') {
      // Ensure value is a positive number
      const intValue = parseInt(value, 10);
      if (isNaN(intValue) || intValue < 1) return;
      
      setFormValues({
        ...formValues,
        rowCount: intValue
      });
    } else {
      setFormValues({
        ...formValues,
        [name]: value
      });
    }
  };
  
  // Handle format selection
  const handleFormatSelect = (format: OutputFormat) => {
    setFormValues({
      ...formValues,
      outputFormat: format
    });
  };
  
  // Generate batch data
  const handleGenerateBatch = async () => {
    if (selectedTemplateIds.length === 0) {
      setError('Please select at least one template');
      return;
    }
    
    setIsGenerating(true);
    setError(null);
    
    try {
      const batchRequest: BatchRequest = {
        templateIds: selectedTemplateIds,
        rowCount: formValues.rowCount,
        outputFormat: formValues.outputFormat,
        parallel: formValues.parallel
      };
      
      const response = await generateBatch(batchRequest);
      
      if (response.error) {
        throw new Error(response.error);
      }
      
      if (onGenerationComplete && response.data) {
        onGenerationComplete(response.data);
      }
    } catch (err) {
      console.error('Error generating batch data:', err);
      setError(err instanceof Error ? err.message : 'Failed to generate batch data');
    } finally {
      setIsGenerating(false);
    }
  };
  
  // Get template display name by ID
  const getTemplateName = (id: number) => {
    const template = templates.find(t => t.id === id);
    return template?.name || `Template ${id}`;
  };
  
  return (
    <div className="batch-generation-form">
      <Card>
        <Card.Header>
          <h2 className="form-section-title">Batch Generation Settings</h2>
        </Card.Header>
        <Card.Body>
          {templatesError && (
            <Alert type="error" className="mb-4">
              Error loading templates: {templatesError}
            </Alert>
          )}
          
          {error && (
            <Alert type="error" className="mb-4">
              {error}
            </Alert>
          )}
          
          {isLoadingTemplates ? (
            <div className="loading-container">
              <div className="loader"></div>
              <p>Loading templates...</p>
            </div>
          ) : (
            <>
              <div className="template-selection-section">
                <div className="section-header">
                  <h3 className="section-title">Select Templates</h3>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={handleSelectAll}
                  >
                    {selectedTemplateIds.length === templates.length ? 'Deselect All' : 'Select All'}
                  </Button>
                </div>
                
                <div className="template-list">
                  {templates.length === 0 ? (
                    <p>No templates available. Please create templates first.</p>
                  ) : (
                    templates.map(template => (
                      <div key={template.id} className="template-checkbox-item">
                        <label className="template-checkbox">
                          <input
                            type="checkbox"
                            checked={selectedTemplateIds.includes(template.id as number)}
                            onChange={() => toggleTemplateSelection(template.id as number)}
                          />
                          <span className="template-name">{template.name}</span>
                          <span className="template-info">
                            {template.columnDefinitions.length} columns
                          </span>
                        </label>
                      </div>
                    ))
                  )}
                </div>
                
                {selectedTemplateIds.length > 0 && (
                  <div className="selected-templates-summary">
                    <p>
                      <strong>{selectedTemplateIds.length}</strong> template(s) selected
                    </p>
                  </div>
                )}
              </div>
              
              <div className="output-settings-section">
                <h3 className="section-title">Output Settings</h3>
                
                <div className="format-options">
                  <div 
                    className={`format-option-card ${formValues.outputFormat === OutputFormat.CSV ? 'selected' : ''}`}
                    onClick={() => handleFormatSelect(OutputFormat.CSV)}
                  >
                    <div className="format-option-icon">
                      <FileText size={24} />
                    </div>
                    <div className="format-option-label">CSV</div>
                  </div>
                  
                  <div 
                    className={`format-option-card ${formValues.outputFormat === OutputFormat.JSON ? 'selected' : ''}`}
                    onClick={() => handleFormatSelect(OutputFormat.JSON)}
                  >
                    <div className="format-option-icon">
                      <FileText size={24} />
                    </div>
                    <div className="format-option-label">JSON</div>
                  </div>
                  
                  <div 
                    className={`format-option-card ${formValues.outputFormat === OutputFormat.XML ? 'selected' : ''}`}
                    onClick={() => handleFormatSelect(OutputFormat.XML)}
                  >
                    <div className="format-option-icon">
                      <FileText size={24} />
                    </div>
                    <div className="format-option-label">XML</div>
                  </div>
                </div>
                
                <div className="form-row">
                  <div className="form-column">
                    <Input
                      label="Row Count (per template)"
                      name="rowCount"
                      type="number"
                      min="1"
                      max="10000"
                      value={formValues.rowCount.toString()}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-column">
                    <div className="execution-mode">
                      <label className="checkbox-label">
                        <input
                          type="checkbox"
                          name="parallel"
                          checked={formValues.parallel}
                          onChange={handleInputChange}
                        />
                        Execute in parallel (faster, but higher resource usage)
                      </label>
                    </div>
                  </div>
                </div>
              </div>
              
              <Button
                className="generate-button"
                variant="primary"
                leftIcon={<Layers size={16} />}
                isLoading={isGenerating}
                onClick={handleGenerateBatch}
                disabled={selectedTemplateIds.length === 0}
              >
                Generate Batch Data
              </Button>
            </>
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default BatchGenerationForm;
