import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Database, 
  FileText, 
  CheckCircle, 
  Download, 
  Table, 
  Code, 
  RefreshCw, 
  Copy,
  AlertTriangle
} from 'react-feather';
import Card from '../../common/Card';
import Button from '../../common/Button';
import Input from '../../common/Input';
import Select from '../../common/Select';
import Alert from '../../common/Alert';
import { Template, OutputFormat } from '../../../models/Template';
import useTemplates from '../../../hooks/useTemplates';
import { generateData, downloadGeneratedData } from '../../../api/generationApi';
import './GenerationForm.css';

interface GenerationFormProps {
  templateId?: number;
  onGenerationComplete?: (data: any, format: OutputFormat) => void;
}

const GenerationForm: React.FC<GenerationFormProps> = ({ 
  templateId,
  onGenerationComplete 
}) => {
  const navigate = useNavigate();
  const { templates, isLoadingTemplates, templatesError, fetchTemplates } = useTemplates();
  
  // Form state
  const [formValues, setFormValues] = useState({
    templateId: templateId || 0,
    rowCount: 100,
    outputFormat: OutputFormat.CSV,
    filename: 'generated_data'
  });
  
  // Generation state
  const [isGenerating, setIsGenerating] = useState(false);
  const [generationResult, setGenerationResult] = useState<{
    success: boolean;
    data: string;
    format: OutputFormat;
    message?: string;
  } | null>(null);
  const [previewType, setPreviewType] = useState<'raw' | 'table'>('table');
  const [error, setError] = useState<string | null>(null);
  
  // Load templates on mount if not passed in
  useEffect(() => {
    if (!templateId) {
      fetchTemplates();
    }
  }, [templateId, fetchTemplates]);
  
  // Update form when templateId prop changes
  useEffect(() => {
    if (templateId) {
      setFormValues(prev => ({
        ...prev,
        templateId
      }));
    }
  }, [templateId]);
  
  // Handle input changes
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    if (name === 'rowCount') {
      // Ensure value is a positive number
      const intValue = parseInt(value, 10);
      if (isNaN(intValue) || intValue < 1) return;
      
      setFormValues({
        ...formValues,
        rowCount: intValue
      });
    } else if (name === 'templateId') {
      const template = templates.find(t => t.id === parseInt(value, 10));
      setFormValues({
        ...formValues,
        templateId: parseInt(value, 10),
        rowCount: template?.defaultRowCount || 100,
        outputFormat: template?.defaultOutputFormat || OutputFormat.CSV,
        filename: template ? `${template.name.toLowerCase().replace(/\s+/g, '_')}_data` : 'generated_data'
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
  
  // Generate data
  const handleGenerate = async () => {
    if (!formValues.templateId) {
      setError('Please select a template');
      return;
    }
    
    setIsGenerating(true);
    setError(null);
    
    try {
      const response = await generateData({
        templateId: formValues.templateId,
        rowCount: formValues.rowCount,
        outputFormat: formValues.outputFormat,
        filename: formValues.filename
      });
      
      if (response.error) {
        throw new Error(response.error);
      }
      
      if (!response.data) {
        throw new Error("No data received from the server");
      }
      
      // Convert blob to text
      const text = await response.data.text();
      
      setGenerationResult({
        success: true,
        data: text,
        format: formValues.outputFormat,
        message: `Successfully generated ${formValues.rowCount} rows of data.`
      });
      
      // Call parent callback if provided
      if (onGenerationComplete) {
        onGenerationComplete(text, formValues.outputFormat);
      }
    } catch (err) {
      console.error('Error generating data:', err);
      setGenerationResult({
        success: false,
        data: '',
        format: formValues.outputFormat,
        message: err instanceof Error ? err.message : 'Failed to generate data'
      });
      setError(err instanceof Error ? err.message : 'Failed to generate data');
    } finally {
      setIsGenerating(false);
    }
  };
  
  // Download generated data
  const downloadData = async () => {
    if (!formValues.templateId) return;
    
    try {
      const success = await downloadGeneratedData({
        templateId: formValues.templateId,
        rowCount: formValues.rowCount,
        outputFormat: formValues.outputFormat,
        filename: formValues.filename
      });
      
      if (!success) {
        throw new Error('Failed to download data');
      }
    } catch (err) {
      console.error('Error downloading data:', err);
      setError(err instanceof Error ? err.message : 'Failed to download data');
    }
  };
  
  // Copy data to clipboard
  const copyToClipboard = () => {
    if (!generationResult?.data) return;
    
    navigator.clipboard.writeText(generationResult.data)
      .then(() => {
        alert('Data copied to clipboard');
      })
      .catch(err => {
        console.error('Error copying to clipboard:', err);
        alert('Failed to copy data to clipboard');
      });
  };
  
  // Format CSV data as table
  const formatCsvPreview = (csvData: string) => {
    const rows = csvData.trim().split('\n');
    if (rows.length === 0) return null;
    
    const headers = rows[0].split(',').map(header => {
      // Remove quotes if present
      return header.replace(/^"(.*)"$/, '$1');
    });
    
    const dataRows = rows.slice(1).map(row => {
      return row.split(',').map(cell => {
        // Remove quotes if present
        return cell.replace(/^"(.*)"$/, '$1');
      });
    });
    
    return (
      <table className="preview-table">
        <thead>
          <tr>
            {headers.map((header, index) => (
              <th key={index}>{header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {dataRows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, cellIndex) => (
                <td key={cellIndex}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  };
  
  // Render preview content
  const renderPreviewContent = () => {
    if (!generationResult) return null;
    
    // Show raw data for JSON, XML, or if raw view is selected
    if (previewType === 'raw' || 
        formValues.outputFormat === OutputFormat.JSON || 
        formValues.outputFormat === OutputFormat.XML) {
      return (
        <pre className="preview-content">
          {generationResult.data}
        </pre>
      );
    }
    
    // Show table view for CSV
    if (formValues.outputFormat === OutputFormat.CSV) {
      return (
        <div className="preview-content">
          {formatCsvPreview(generationResult.data)}
        </div>
      );
    }
    
    return null;
  };
  
  // Create template options for select
  const templateOptions = templates.map(template => ({
    value: template.id?.toString() || '',
    label: template.name
  }));
  
  return (
    <div className="generation-form-container">
      <div className="generation-form">
        <Card>
          <Card.Header>
            <h2 className="form-section-title">Generation Settings</h2>
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
                <div className="form-group">
                  <Select
                    label="Select Template"
                    name="templateId"
                    value={formValues.templateId.toString()}
                    onChange={handleInputChange}
                    options={templateOptions}
                    placeholder="Select a template..."
                    required
                  />
                </div>
                
                <div className="form-section">
                  <h3 className="form-section-title">Output Settings</h3>
                  
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
                        <Code size={24} />
                      </div>
                      <div className="format-option-label">JSON</div>
                    </div>
                    
                    <div 
                      className={`format-option-card ${formValues.outputFormat === OutputFormat.XML ? 'selected' : ''}`}
                      onClick={() => handleFormatSelect(OutputFormat.XML)}
                    >
                      <div className="format-option-icon">
                        <Code size={24} />
                      </div>
                      <div className="format-option-label">XML</div>
                    </div>
                  </div>
                  
                  <div className="form-row">
                    <div className="form-column">
                      <Input
                        label="Row Count"
                        name="rowCount"
                        type="number"
                        min="1"
                        max="10000"
                        value={formValues.rowCount.toString()}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-column">
                      <Input
                        label="Filename"
                        name="filename"
                        value={formValues.filename}
                        onChange={handleInputChange}
                      />
                    </div>
                  </div>
                </div>
                
                <Button
                  className="generate-button"
                  variant="primary"
                  leftIcon={<Database size={16} />}
                  isLoading={isGenerating}
                  onClick={handleGenerate}
                  disabled={!formValues.templateId}
                >
                  Generate Data
                </Button>
              </>
            )}
          </Card.Body>
        </Card>
      </div>
      
      <div className="preview-section">
        <Card>
          <Card.Header>
            <div className="preview-header">
              <h2 className="preview-title">Data Preview</h2>
              {generationResult && generationResult.format === OutputFormat.CSV && (
                <div className="preview-toggle">
                  <Button
                    variant={previewType === 'table' ? 'primary' : 'outline'}
                    size="sm"
                    leftIcon={<Table size={16} />}
                    onClick={() => setPreviewType('table')}
                  >
                    Table
                  </Button>
                  <Button
                    variant={previewType === 'raw' ? 'primary' : 'outline'}
                    size="sm"
                    leftIcon={<Code size={16} />}
                    onClick={() => setPreviewType('raw')}
                  >
                    Raw
                  </Button>
                </div>
              )}
            </div>
          </Card.Header>
          <Card.Body>
            {isGenerating ? (
              <div className="loading-container">
                <div className="loader"></div>
                <p>Generating data...</p>
              </div>
            ) : generationResult ? (
              <div className="generation-result">
                {generationResult.success ? (
                  <div className="generation-success">
                    {renderPreviewContent()}
                    
                    <div className="preview-actions">
                      <Button
                        variant="outline"
                        size="sm"
                        leftIcon={<Copy size={16} />}
                        onClick={copyToClipboard}
                      >
                        Copy to Clipboard
                      </Button>
                      <Button
                        variant="primary"
                        size="sm"
                        leftIcon={<Download size={16} />}
                        onClick={downloadData}
                      >
                        Download
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="generation-error">
                    <AlertTriangle size={48} className="error-icon" />
                    <h3>Generation Failed</h3>
                    <p>{generationResult.message}</p>
                  </div>
                )}
              </div>
            ) : (
              <div className="preview-empty">
                <Database size={48} color="var(--jpm-neutral-400)" />
                <p>Select a template and generate data to see a preview.</p>
              </div>
            )}
          </Card.Body>
        </Card>
      </div>
    </div>
  );
};

export default GenerationForm;
