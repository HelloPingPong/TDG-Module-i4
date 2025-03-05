import React, { useState } from 'react';
import { 
  Edit, 
  Save, 
  Database, 
  Trash2, 
  ArrowDown, 
  ArrowUp, 
  Plus 
} from 'react-feather';
import Card from '../../common/Card';
import Button from '../../common/Button';
import Select from '../../common/Select';
import Input from '../../common/Input';
import { Template } from '../../../models/Template';
import { ColumnDefinition, DataTypeMetadata } from '../../../models/ColumnDefinition';
import useDataTypes from '../../../hooks/useDataTypes';

interface TemplatePreviewProps {
  template: Template;
  isEditable?: boolean;
  onSave?: (template: Template) => void;
  onGenerate?: () => void;
}

const TemplatePreview: React.FC<TemplatePreviewProps> = ({
  template,
  isEditable = false,
  onSave,
  onGenerate
}) => {
  const { dataTypes, dataTypeOptions, isLoading: isLoadingDataTypes, getDataTypeMetadata } = useDataTypes({ immediate: true });
  
  // State for editable template
  const [editedTemplate, setEditedTemplate] = useState<Template>(template);
  const [expandedColumnId, setExpandedColumnId] = useState<number | null>(null);
  
  // Handle column expansion toggle
  const toggleColumnExpansion = (columnId: number) => {
    if (expandedColumnId === columnId) {
      setExpandedColumnId(null);
    } else {
      setExpandedColumnId(columnId);
    }
  };
  
  // Handle template metadata changes
  const handleTemplateMetadataChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    if (!isEditable) return;
    
    const { name, value } = e.target;
    setEditedTemplate(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // Handle column data changes
  const handleColumnChange = (columnId: number, field: string, value: any) => {
    if (!isEditable) return;
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: prev.columnDefinitions.map(col => {
        if (col.id === columnId) {
          return {
            ...col,
            [field]: value
          };
        }
        return col;
      })
    }));
  };
  
  // Handle column constraint changes
  const handleConstraintChange = (columnId: number, constraintName: string, value: any) => {
    if (!isEditable) return;
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: prev.columnDefinitions.map(col => {
        if (col.id === columnId) {
          return {
            ...col,
            constraints: {
              ...col.constraints,
              [constraintName]: value
            }
          };
        }
        return col;
      })
    }));
  };
  
  // Add new column
  const addColumn = () => {
    if (!isEditable) return;
    
    const newColumn: ColumnDefinition = {
      id: Date.now(), // Temporary ID for UI purposes
      name: `column${editedTemplate.columnDefinitions.length + 1}`,
      type: 'string',
      sequenceNumber: editedTemplate.columnDefinitions.length + 1,
      isNullable: false,
      nullProbability: 0,
      constraints: {}
    };
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: [...prev.columnDefinitions, newColumn]
    }));
    
    // Expand the new column automatically
    setExpandedColumnId(newColumn.id);
  };
  
  // Remove column
  const removeColumn = (columnId: number) => {
    if (!isEditable) return;
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: prev.columnDefinitions
        .filter(col => col.id !== columnId)
        .map((col, index) => ({
          ...col,
          sequenceNumber: index + 1
        }))
    }));
    
    if (expandedColumnId === columnId) {
      setExpandedColumnId(null);
    }
  };
  
  // Move column up
  const moveColumnUp = (columnId: number) => {
    if (!isEditable) return;
    
    const columnIndex = editedTemplate.columnDefinitions.findIndex(col => col.id === columnId);
    if (columnIndex <= 0) return;
    
    const updatedColumns = [...editedTemplate.columnDefinitions];
    const temp = updatedColumns[columnIndex];
    updatedColumns[columnIndex] = updatedColumns[columnIndex - 1];
    updatedColumns[columnIndex - 1] = temp;
    
    // Update sequence numbers
    updatedColumns.forEach((col, index) => {
      col.sequenceNumber = index + 1;
    });
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: updatedColumns
    }));
  };
  
  // Move column down
  const moveColumnDown = (columnId: number) => {
    if (!isEditable) return;
    
    const columnIndex = editedTemplate.columnDefinitions.findIndex(col => col.id === columnId);
    if (columnIndex >= editedTemplate.columnDefinitions.length - 1) return;
    
    const updatedColumns = [...editedTemplate.columnDefinitions];
    const temp = updatedColumns[columnIndex];
    updatedColumns[columnIndex] = updatedColumns[columnIndex + 1];
    updatedColumns[columnIndex + 1] = temp;
    
    // Update sequence numbers
    updatedColumns.forEach((col, index) => {
      col.sequenceNumber = index + 1;
    });
    
    setEditedTemplate(prev => ({
      ...prev,
      columnDefinitions: updatedColumns
    }));
  };
  
  // Save template changes
  const handleSave = () => {
    if (onSave) {
      onSave(editedTemplate);
    }
  };
  
  // Get constraints for a data type
  const getConstraintsForType = (type: string) => {
    const metadata = getDataTypeMetadata(type);
    if (!metadata || !metadata.constraintsMetadata) return [];
    
    return Object.entries(metadata.constraintsMetadata).map(([key, description]) => ({
      name: key,
      label: key.charAt(0).toUpperCase() + key.slice(1),
      description: description as string,
      type: inferConstraintType(description as string)
    }));
  };
  
  // Infer constraint type from description
  const inferConstraintType = (description: string): string => {
    const desc = description.toLowerCase();
    if (desc.includes('number') || desc.includes('length') || desc.includes('count')) {
      return 'number';
    } else if (desc.includes('boolean') || desc.includes('flag')) {
      return 'checkbox';
    } else if (desc.includes('select') || desc.includes('options') || desc.includes('choose')) {
      return 'select';
    } else if (desc.includes('date')) {
      return 'date';
    } else {
      return 'text';
    }
  };
  
  // Get human-readable type name
  const getTypeName = (type: string): string => {
    const metadata = getDataTypeMetadata(type);
    return metadata?.displayName || type;
  };
  
  return (
    <div className="template-preview">
      <Card>
        <Card.Header>
          <div className="template-header">
            <h2 className="template-title">
              {isEditable ? 'Edit Template' : 'Template Preview'}
            </h2>
            <div className="template-actions">
              {isEditable && (
                <Button
                  variant="primary"
                  leftIcon={<Save size={16} />}
                  onClick={handleSave}
                >
                  Save Template
                </Button>
              )}
              
              {onGenerate && (
                <Button
                  variant="outline"
                  leftIcon={<Database size={16} />}
                  onClick={onGenerate}
                >
                  Generate Data
                </Button>
              )}
            </div>
          </div>
        </Card.Header>
        <Card.Body>
          <div className="template-metadata-section">
            <h3 className="section-title">Template Information</h3>
            
            <div className="form-row">
              <div className="form-column">
                <Input
                  label="Template Name"
                  name="name"
                  value={editedTemplate.name}
                  onChange={handleTemplateMetadataChange}
                  disabled={!isEditable}
                  required
                />
              </div>
              
              <div className="form-column">
                <div className="form-group">
                  <label htmlFor="defaultOutputFormat" className="input-label">Default Output Format</label>
                  <select
                    id="defaultOutputFormat"
                    name="defaultOutputFormat"
                    value={editedTemplate.defaultOutputFormat}
                    onChange={handleTemplateMetadataChange}
                    disabled={!isEditable}
                    className="input-select"
                  >
                    <option value="CSV">CSV</option>
                    <option value="JSON">JSON</option>
                    <option value="XML">XML</option>
                  </select>
                </div>
              </div>
            </div>
            
            <div className="form-row">
              <div className="form-column">
                <div className="form-group">
                  <label htmlFor="description" className="input-label">Description</label>
                  <textarea
                    id="description"
                    name="description"
                    value={editedTemplate.description}
                    onChange={handleTemplateMetadataChange}
                    disabled={!isEditable}
                    className="input-textarea"
                    rows={3}
                  />
                </div>
              </div>
              
              <div className="form-column">
                <Input
                  label="Default Row Count"
                  name="defaultRowCount"
                  type="number"
                  min="1"
                  max="10000"
                  value={editedTemplate.defaultRowCount.toString()}
                  onChange={handleTemplateMetadataChange}
                  disabled={!isEditable}
                />
              </div>
            </div>
          </div>
          
          <div className="template-columns-section">
            <div className="section-header">
              <h3 className="section-title">Columns ({editedTemplate.columnDefinitions.length})</h3>
              {isEditable && (
                <Button
                  variant="outline"
                  size="sm"
                  leftIcon={<Plus size={16} />}
                  onClick={addColumn}
                >
                  Add Column
                </Button>
              )}
            </div>
            
            {editedTemplate.columnDefinitions.length === 0 ? (
              <div className="no-columns">
                <p>No columns defined for this template.</p>
              </div>
            ) : (
              <div className="columns-list">
                {editedTemplate.columnDefinitions.map((column, index) => (
                  <div className="column-item" key={column.id}>
                    <div 
                      className="column-header"
                      onClick={() => toggleColumnExpansion(column.id as number)}
                    >
                      <div className="column-info">
                        <span className="column-number">{index + 1}</span>
                        <span className="column-name">{column.name}</span>
                        <span className="column-type">({getTypeName(column.type)})</span>
                      </div>
                      
                      <div className="column-actions">
                        {isEditable && (
                          <>
                            <button
                              className="column-action"
                              onClick={(e) => {
                                e.stopPropagation();
                                moveColumnUp(column.id as number);
                              }}
                              disabled={index === 0}
                              title="Move Up"
                            >
                              <ArrowUp size={16} />
                            </button>
                            <button
                              className="column-action"
                              onClick={(e) => {
                                e.stopPropagation();
                                moveColumnDown(column.id as number);
                              }}
                              disabled={index === editedTemplate.columnDefinitions.length - 1}
                              title="Move Down"
                            >
                              <ArrowDown size={16} />
                            </button>
                            <button
                              className="column-action delete"
                              onClick={(e) => {
                                e.stopPropagation();
                                removeColumn(column.id as number);
                              }}
                              title="Remove Column"
                            >
                              <Trash2 size={16} />
                            </button>
                          </>
                        )}
                        <button className="column-expand">
                          {expandedColumnId === column.id ? 'Collapse' : 'Expand'}
                        </button>
                      </div>
                    </div>
                    
                    {expandedColumnId === column.id && (
                      <div className="column-details">
                        <div className="form-row">
                          <div className="form-column">
                            <Input
                              label="Column Name"
                              value={column.name}
                              onChange={(e) => handleColumnChange(column.id as number, 'name', e.target.value)}
                              disabled={!isEditable}
                              required
                            />
                          </div>
                          
                          <div className="form-column">
                            <div className="form-group">
                              <label className="input-label">Data Type</label>
                              <Select
                                value={column.type}
                                onChange={(e) => handleColumnChange(column.id as number, 'type', e.target.value)}
                                options={dataTypeOptions}
                                disabled={!isEditable}
                                placeholder="Select a data type..."
                              />
                            </div>
                          </div>
                        </div>
                        
                        <div className="form-row">
                          <div className="form-column">
                            <div className="form-group">
                              <label className="checkbox-label">
                                <input
                                  type="checkbox"
                                  checked={column.isNullable}
                                  onChange={(e) => handleColumnChange(column.id as number, 'isNullable', e.target.checked)}
                                  disabled={!isEditable}
                                />
                                Allow NULL values
                              </label>
                            </div>
                          </div>
                          
                          {column.isNullable && (
                            <div className="form-column">
                              <Input
                                label="NULL Probability (0-1)"
                                type="number"
                                min="0"
                                max="1"
                                step="0.1"
                                value={column.nullProbability.toString()}
                                onChange={(e) => handleColumnChange(
                                  column.id as number, 
                                  'nullProbability', 
                                  parseFloat(e.target.value)
                                )}
                                disabled={!isEditable}
                              />
                            </div>
                          )}
                        </div>
                        
                        {getConstraintsForType(column.type).length > 0 && (
                          <div className="column-constraints">
                            <h4 className="constraints-title">Constraints</h4>
                            
                            <div className="constraints-grid">
                              {getConstraintsForType(column.type).map(constraint => (
                                <div className="constraint-item" key={constraint.name}>
                                  {constraint.type === 'checkbox' ? (
                                    <div className="form-group">
                                      <label className="checkbox-label">
                                        <input
                                          type="checkbox"
                                          checked={!!column.constraints[constraint.name]}
                                          onChange={(e) => handleConstraintChange(
                                            column.id as number,
                                            constraint.name,
                                            e.target.checked
                                          )}
                                          disabled={!isEditable}
                                        />
                                        {constraint.label}
                                      </label>
                                    </div>
                                  ) : constraint.type === 'select' ? (
                                    <div className="form-group">
                                      <label className="input-label">{constraint.label}</label>
                                      <select
                                        value={column.constraints[constraint.name] || ''}
                                        onChange={(e) => handleConstraintChange(
                                          column.id as number,
                                          constraint.name,
                                          e.target.value
                                        )}
                                        disabled={!isEditable}
                                        className="input-select"
                                      >
                                        <option value="">Select...</option>
                                        {/* Options would be populated based on the constraint type */}
                                        <option value="option1">Option 1</option>
                                        <option value="option2">Option 2</option>
                                      </select>
                                    </div>
                                  ) : (
                                    <Input
                                      label={constraint.label}
                                      type={constraint.type}
                                      value={column.constraints[constraint.name] || ''}
                                      onChange={(e) => handleConstraintChange(
                                        column.id as number,
                                        constraint.name,
                                        e.target.value
                                      )}
                                      disabled={!isEditable}
                                      title={constraint.description}
                                    />
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default TemplatePreview;
