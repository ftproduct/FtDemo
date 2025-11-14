import { useState, useEffect } from 'react';
import './styles/globals.css';
import ComponentGallery from './components/pages/ComponentGallery';
import InTransitPage from './components/pages/InTransitPage';

export default function App() {
  const [currentPage, setCurrentPage] = useState('components');
  const [theme, setTheme] = useState<'light' | 'dark' | 'night'>('light');

  useEffect(() => {
    // Apply theme to html element
    document.documentElement.className = theme;
  }, [theme]);

  useEffect(() => {
    // Simple client-side routing based on pathname
    const path = window.location.pathname;
    if (path.includes('myjourneys')) {
      setCurrentPage('myjourneys');
    } else if (path.includes('components')) {
      setCurrentPage('components');
    } else {
      setCurrentPage('myjourneys'); // default to myjourneys
    }

    // Listen for navigation events
    const handlePopState = () => {
      const path = window.location.pathname;
      if (path.includes('myjourneys')) {
        setCurrentPage('myjourneys');
      } else if (path.includes('components')) {
        setCurrentPage('components');
      } else {
        setCurrentPage('myjourneys');
      }
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const cycleTheme = () => {
    setTheme((current) => {
      if (current === 'light') return 'dark';
      if (current === 'dark') return 'night';
      return 'light';
    });
  };

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-secondary)' }}>
      {/* Page Content */}
      {currentPage === 'components' && <ComponentGallery />}
      {currentPage === 'myjourneys' && <InTransitPage />}
    </div>
  );
}
