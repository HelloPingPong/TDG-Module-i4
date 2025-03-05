import React, { useState } from 'react';
import { 
  Download, 
  CheckCircle, 
  AlertTriangle, 
  RefreshCw, 
  Clock, 
  File, 
  Layers 
} from 'react-feather';
import Card from '../../common/Card';
import Button from '../../common/Button';
import Alert from '../../common/Alert';
import { BatchResult, BatchItemResult, BatchStatus } from '../../../models/BatchRequest';
import { downloadBatchResults } from '../../../api/batchApi';

interface BatchResultsProps {
  results: BatchResult;
  isLoading?: boolean;
  onRegenerate?: () => void;
}

const BatchResults: React.FC<BatchResultsProps> = ({
  results,
  isLoading = false,
  onRegenerate
}) => {
  const [expandedItemId, setExpandedItemId] = useState<number | null>(null);
  
  // Handle item expansion toggle
  const toggleItemExpansion = (itemId: number) => {
    if (expandedItemId === itemId) {
      setExpandedItemId(null);
    } else {
      setExpandedItemId(itemId);
    }
  };
  
  // Download all results as ZIP
  const handleDownloadAll = async () => {
    if (!results.batchId) return;
    
    try {
      const success = await downloadBatchResults(results.batchId);
      
      if (!success) {
        throw new Error('Failed to download batch results');
      }
    } catch (err) {
      console.error('Error downloading batch results:', err);
      alert('Failed to download batch results');
    }
  };
  
  // Format duration in milliseconds to seconds with 2 decimal places
  const formatDuration = (ms: number) => {
    return (ms / 1000).toFixed(2) + ' seconds';
  };
  
  // Format timestamp
  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };
  
  // Get status icon based on status
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle size={16} className="status-icon success" />;
      case 'FAILED':
        return <AlertTriangle size={16} className="status-icon error" />;
      case 'PROCESSING':
        return <RefreshCw size={16} className="status-icon processing" />;
      case 'PENDING':
        return <Clock size={16} className="status-icon pending" />;
      default:
        return null;
    }
  };
  
  // Calculate batch summary
  const calculateSummary = () => {
    if (!results.results) return { total: 0, success: 0, failed: 0 };
    
    const total = results.results.length;
    const success = results.results.filter(r => r.success).length;
    const failed = total - success;
    
    return { total, success, failed };
  };
  
  // Render loading state
  if (isLoading) {
    return (
      <div className="batch-results batch-results-loading">
        <div className="loading-indicator">
          <div className="spinner"></div>
          <p>Processing batch generation...</p>
        </div>
      </div>
    );
  }
  
  // Render error state if no results
  if (!results || !results.results) {
    return (
      <div className="batch-results batch-results-error">
        <Alert type="error">
          No batch results available
        </Alert>
        
        {onRegenerate && (
          <Button
            variant="primary"
            leftIcon={<RefreshCw size={16} />}
            onClick={onRegenerate}
          >
            Try Again
          </Button>
        )}
      </div>
    );
  }
  
  const summary = calculateSummary();
  
  return (
    <div className="batch-results">
      <Card>
        <Card.Header>
          <div className="results-header">
            <h2 className="results-title">Batch Generation Results</h2>
            <div className="batch-summary">
              <div className="batch-stats">
                <span>Total: {summary.total}</span>
                <span className="success-count">Success: {summary.success}</span>
                {summary.failed > 0 && (
                  <span className="failed-count">Failed: {summary.failed}</span>
                )}
              </div>
              
              {results.totalDurationMs && (
                <div className="batch-duration">
                  Total Time: {formatDuration(results.totalDurationMs)}
                </div>
              )}
            </div>
          </div>
        </Card.Header>
        <Card.Body>
          <div className="batch-status-bar">
            <div className="batch-status">
              <span className="status-label">Status:</span>
              <span className={`status-value status-${results.status.toLowerCase()}`}>
                {getStatusIcon(results.status)}
                {results.status}
              </span>
            </div>
            
            {results.startTime && (
              <div className="batch-timestamp">
                <span className="timestamp-label">Started:</span>
                <span className="timestamp-value">{formatTimestamp(results.startTime)}</span>
              </div>
            )}
            
            {results.endTime && (
              <div className="batch-timestamp">
                <span className="timestamp-label">Completed:</span>
                <span className="timestamp-value">{formatTimestamp(results.endTime)}</span>
              </div>
            )}
          </div>
          
          <div className="results-list">
            {results.results.map((result: BatchItemResult) => (
              <div 
                key={result.templateId} 
                className={`result-item ${result.success ? 'success' : 'failed'}`}
              >
                <div 
                  className="result-item-header"
                  onClick={() => toggleItemExpansion(result.templateId)}
                >
                  <div className="result-item-title">
                    <span className="result-item-icon">
                      {getStatusIcon(result.status)}
                    </span>
                    <span className="result-item-name">
                      {result.templateName || `Template ${result.templateId}`}
                    </span>
                  </div>
                  
                  <div className="result-item-meta">
                    <span className="result-item-format">{result.outputFormat}</span>
                    {result.durationMs && (
                      <span className="result-item-duration">
                        {formatDuration(result.durationMs)}
                      </span>
                    )}
                    <button className="expand-button">
                      {expandedItemId === result.templateId ? 'Collapse' : 'Expand'}
                    </button>
                  </div>
                </div>
                
                {expandedItemId === result.templateId && (
                  <div className="result-item-details">
                    <div className="result-item-message">
                      {result.message}
                    </div>
                    
                    {result.dataPreview && (
                      <div className="result-preview">
                        <h4>Data Preview</h4>
                        <pre className="preview-content">{result.dataPreview}</pre>
                      </div>
                    )}
                    
                    {result.success && result.downloadUrl && (
                      <div className="result-actions">
                        <Button
                          variant="outline"
                          size="sm"
                          leftIcon={<Download size={16} />}
                          component="a"
                          href={result.downloadUrl}
                          download
                        >
                          Download File
                        </Button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
          
          <div className="batch-actions">
            {results.downloadUrl && (
              <Button
                variant="primary"
                leftIcon={<Download size={16} />}
                onClick={handleDownloadAll}
              >
                Download All Files (ZIP)
              </Button>
            )}
            
            {onRegenerate && (
              <Button
                variant="outline"
                leftIcon={<RefreshCw size={16} />}
                onClick={onRegenerate}
              >
                Generate New Batch
              </Button>
            )}
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default BatchResults;
