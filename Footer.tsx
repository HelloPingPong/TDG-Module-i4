import React from 'react';
import { Link } from 'react-router-dom';
import '../layout/MainLayout.css';

interface FooterProps {
  companyName?: string;
  version?: string;
  links?: Array<{
    name: string;
    url: string;
  }>;
}

const Footer: React.FC<FooterProps> = ({
  companyName = 'Your Company',
  version = '1.0.0',
  links
}) => {
  const currentYear = new Date().getFullYear();
  
  return (
    <footer className="jpm-footer">
      <div className="jpm-footer-content">
        <div className="jpm-footer-left">
          <p>© {currentYear} {companyName}. All rights reserved.</p>
        </div>
        
        {links && links.length > 0 && (
          <div className="jpm-footer-center">
            <ul className="jpm-footer-links">
              {links.map((link, index) => (
                <li key={index}>
                  <Link to={link.url}>{link.name}</Link>
                </li>
              ))}
            </ul>
          </div>
        )}
        
        <div className="jpm-footer-right">
          <p>Test Data Generator v{version}</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
