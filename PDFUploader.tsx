import React, { useRef, useState } from 'react';
import { 
  Upload, 
  X, 
  File, 
  CheckCircle, 
  AlertTriangle 
} from 'react-feather';
import Card from '../../common/Card';
import Button from '../../common/Button';
import Alert from '../../common/Alert';
import { analyzePdf } from '../../../api/pdfApi';
import { Template } from '../../../models/Template';

interface PDFUploaderProps {
  onPdfAnalyzed?: (template: Template) => void;
  onProgress?: (progress: number) => void;
}

const PDFUploader: React.FC<PDFUploaderProps> = ({ 
  onPdfAnalyzed,
  onProgress
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [analysisSteps, setAnalysisSteps] = useState<string[]>([
    'Loading PDF document',
    'Extracting text content',
    'Detecting variables',
    'Analyzing variable types',
    'Generating template'
  ]);
  const [currentStep, setCurrentStep] = useState<number | null>(null);
  
  // Handle file selection from input
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (file.type === 'application/pdf') {
        setSelectedFile(file);
        setError(null);
      } else {
        setError('Only PDF files are allowed.');
      }
    }
  };
  
  // Handle drag over event
  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
  };
  
  // Handle drop event
  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (file.type === 'application/pdf') {
        setSelectedFile(file);
        setError(null);
      } else {
        setError('Only PDF files are allowed.');
      }
    }
  };
  
  // Handle click on upload area
  const handleUploadAreaClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };
  
  // Handle file removal
  const handleFileRemove = () => {
    setSelectedFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    setCurrentStep(null);
    setUploadProgress(0);
  };
  
  // Format file size for display
  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };
  
  // Upload and analyze the PDF
  const handleAnalyze = async () => {
    if (!selectedFile) {
      setError('Please select a PDF file first.');
      return;
    }
    
    setIsUploading(true);
    setError(null);
    setCurrentStep(0);
    
    try {
      // Simulate analysis steps with progress
      for (let i = 0; i < analysisSteps.length; i++) {
        setCurrentStep(i);
        const progress = Math.round((i / (analysisSteps.length - 1)) * 100);
        setUploadProgress(progress);
        if (onProgress) {
          onProgress(progress);
        }
        
        // Simulate step processing time
        await new Promise(resolve => setTimeout(resolve, 700));
      }
      
      // Call the actual API
      const response = await analyzePdf(selectedFile);
      
      if (response.error) {
        throw new Error(response.error);
      }
      
      if (!response.data) {
        throw new Error('No template data returned from analysis');
      }
      
      if (onPdfAnalyzed) {
        onPdfAnalyzed(response.data);
      }
      
      setCurrentStep(analysisSteps.length - 1);
      setUploadProgress(100);
    } catch (err) {
      console.error('Error analyzing PDF:', err);
      setError(err instanceof Error ? err.message : 'Failed to analyze PDF');
      setCurrentStep(null);
    } finally {
      setIsUploading(false);
    }
  };
  
  return (
    <div className="pdf-uploader">
      <Card>
        <Card.Header>
          <h2 className="uploader-title">Upload Redline PDF</h2>
        </Card.Header>
        <Card.Body>
          {error && (
            <Alert type="error" className="mb-4">
              {error}
            </Alert>
          )}
          
          {!selectedFile ? (
            <div 
              className="upload-area"
              onDragOver={handleDragOver}
              onDrop={handleDrop}
              onClick={handleUploadAreaClick}
            >
              <Upload size={48} className="upload-icon" />
              <h3 className="upload-text">Click to upload or drag and drop</h3>
              <p className="upload-subtext">PDF files only (Max 10MB)</p>
              <input
                type="file"
                ref={fileInputRef}
                className="file-input"
                accept="application/pdf"
                onChange={handleFileSelect}
              />
            </div>
          ) : (
            <div className="selected-file">
              <div className="file-info">
                <File size={24} className="file-icon" />
                <div className="file-details">
                  <div className="file-name">{selectedFile.name}</div>
                  <div className="file-size">{formatFileSize(selectedFile.size)}</div>
                </div>
                <button
                  className="file-remove-button"
                  onClick={handleFileRemove}
                  disabled={isUploading}
                  title="Remove file"
                >
                  <X size={16} />
                </button>
              </div>
              
              {isUploading ? (
                <div className="analysis-progress">
                  <div className="progress-bar-container">
                    <div 
                      className="progress-bar" 
                      style={{ width: `${uploadProgress}%` }}
                    ></div>
                  </div>
                  
                  <div className="analysis-steps">
                    {analysisSteps.map((step, index) => (
                      <div
                        key={index}
                        className={`analysis-step ${
                          currentStep === index
                            ? 'active'
                            : currentStep !== null && currentStep > index
                            ? 'completed'
                            : ''
                        }`}
                      >
                        {currentStep !== null && currentStep > index ? (
                          <CheckCircle size={16} className="step-icon completed" />
                        ) : (
                          <div className={`step-number ${currentStep === index ? 'active' : ''}`}>
                            {index + 1}
                          </div>
                        )}
                        <span className="step-text">{step}</span>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <Button
                  className="analyze-button"
                  variant="primary"
                  onClick={handleAnalyze}
                >
                  Analyze PDF
                </Button>
              )}
            </div>
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default PDFUploader;
