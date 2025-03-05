import React, { useState } from 'react';
import { 
  Download, 
  Copy, 
  Table, 
  Code, 
  CheckCircle, 
  AlertTriangle, 
  RefreshCw, 
  FileText 
} from 'react-feather';
import Button from '../../common/Button';
import Alert from '../../common/Alert';
import { OutputFormat } from '../../../models/Template';
import './GenerationResult.css';

export interface GenerationResultProps {
  data: string;
  format: OutputFormat;
  isSuccess: boolean;
  isLoading?: boolean;
  message?: string;
  filename?: string;
  rowCount?: number;
  columnCount?: number;
  onDownload?: () => void;
  onRegenerate?: () => void;
}

const GenerationResult: React.FC<GenerationResultProps> = ({
  data,
  format,
  isSuccess,
  isLoading = false,
  message,
  filename = 'generated_data',
  rowCount,
  columnCount,
  onDownload,
  onRegenerate
}) => {
  const [viewMode, setViewMode] = useState<'table' | 'raw'>(format === OutputFormat.CSV ? 'table' : 'raw');
  
  // Copy data to clipboard
  const copyToClipboard = () => {
    if (!data) return;
    
    navigator.clipboard.writeText(data)
      .then(() => {
        alert('Data copied to clipboard');
      })
      .catch(err => {
        console.error('Error copying to clipboard:', err);
        alert('Failed to copy data to clipboard');
      });
  };
  
  // Download data (if no handler provided)
  const handleDownload = () => {
    if (onDownload) {
      onDownload();
      return;
    }
    
    if (!data) return;
    
    const blob = new Blob([data], { 
      type: getContentType(format) 
    });
    const url = URL.createObjectURL(blob);
    const extension = format.toLowerCase();
    const downloadFilename = `${filename}.${extension}`;
    
    const a = document.createElement('a');
    a.href = url;
    a.download = downloadFilename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };
  
  // Get content type based on format
  const getContentType = (format: OutputFormat): string => {
    switch (format) {
      case OutputFormat.CSV:
        return 'text/csv';
      case OutputFormat.JSON:
        return 'application/json';
      case OutputFormat.XML:
        return 'application/xml';
      default:
        return 'text/plain';
    }
  };
  
  // Format CSV data as table
  const formatCsvTable = (csvData: string) => {
    if (!csvData) return null;
    
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
      <table className="result-table">
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
  
  // Format JSON data for display
  const formatJsonDisplay = (jsonData: string) => {
    try {
      const parsedData = JSON.parse(jsonData);
      return JSON.stringify(parsedData, null, 2);
    } catch (e) {
      return jsonData;
    }
  };
  
  // Render content based on format and view mode
  const renderContent = () => {
    if (!data) return null;
    
    if (format === OutputFormat.CSV && viewMode === 'table') {
      return (
        <div className="result-content">
          {formatCsvTable(data)}
        </div>
      );
    } else if (format === OutputFormat.JSON) {
      return (
        <pre className="result-content">
          {formatJsonDisplay(data)}
        </pre>
      );
    } else {
      return (
        <pre className="result-content">
          {data}
        </pre>
      );
    }
  };
  
  if (isLoading) {
    return (
      <div className="generation-result generation-loading">
        <div className="result-loading">
          <div className="spinner"></div>
          <p>Generating data...</p>
        </div>
      </div>
    );
  }
  
  if (!isSuccess) {
    return (
      <div className="generation-result generation-error">
        <div className="result-error">
          <AlertTriangle size={48} className="error-icon" />
          <h3>Generation Failed</h3>
          {message && <p>{message}</p>}
          
          {onRegenerate && (
            <Button
              variant="primary"
              size="sm"
              leftIcon={<RefreshCw size={16} />}
              onClick={onRegenerate}
            >
              Try Again
            </Button>
          )}
        </div>
      </div>
    );
  }
  
  return (
    <div className="generation-result generation-success">
      <div className="result-header">
        <div className="result-info">
          <div className="result-format">
            <FileText size={16} />
            <span>{format}</span>
          </div>
          
          {rowCount && columnCount && (
            <div className="result-stats">
              <span>{rowCount} rows</span>
              <span>•</span>
              <span>{columnCount} columns</span>
            </div>
          )}
        </div>
        
        {format === OutputFormat.CSV && (
          <div className="view-toggle">
            <Button
              variant={viewMode === 'table' ? 'primary' : 'outline'}
              size="sm"
              leftIcon={<Table size={16} />}
              onClick={() => setViewMode('table')}
            >
              Table
            </Button>
            <Button
              variant={viewMode === 'raw' ? 'primary' : 'outline'}
              size="sm"
              leftIcon={<Code size={16} />}
              onClick={() => setViewMode('raw')}
            >
              Raw
            </Button>
          </div>
        )}
      </div>
      
      {message && (
        <Alert type="success" className="result-message">
          <CheckCircle size={16} /> {message}
        </Alert>
      )}
      
      {renderContent()}
      
      <div className="result-actions">
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
          onClick={handleDownload}
        >
          Download
        </Button>
        
        {onRegenerate && (
          <Button
            variant="outline"
            size="sm"
            leftIcon={<RefreshCw size={16} />}
            onClick={onRegenerate}
          >
            Regenerate
          </Button>
        )}
      </div>
    </div>
  );
};

export default GenerationResult;
