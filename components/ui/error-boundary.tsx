'use client';

/**
 * ErrorBoundary - Client-side error boundary component
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

import React from 'react';
import { Button } from 'ft-design-system';

interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error?: Error; resetError: () => void }>;
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  resetError = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        const Fallback = this.props.fallback;
        return <Fallback error={this.state.error} resetError={this.resetError} />;
      }

      return (
        <div className="p-8 flex flex-col items-center justify-center min-h-screen gap-4">
          <h2 className="text-xl-rem font-semibold text-critical">
            Something went wrong
          </h2>
          {this.state.error && (
            <p className="text-sm-rem text-neutral-500 text-center">
              {this.state.error.message}
            </p>
          )}
          <Button variant="primary" size="md" onClick={this.resetError}>
            Try again
          </Button>
        </div>
      );
    }

    return this.props.children;
  }
}
