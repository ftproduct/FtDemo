import React, { useState } from 'react';
import { Badge, Table, ProgressBar, Tabs, Checkbox, RadioGroup, Switch, Button, Input, Card, Statistic, Text, SubText, DisplayBlock, NavigationMenu, QuickFilters, Dropdown, DatePicker, AppHeader, Footer, UserProfile, Collapsible, UploadZone, FileCard, FileThumbnail, FileTypeIcon, Steps, RadioSelector, SegmentedTabs, Typography, ButtonGroup, Spacer, Divider, UserProfileDropdown, ListingLayout } from 'ft-design-system/ai';
import { MissingComponent } from '../MissingComponent';
import { Clock } from 'lucide-react';

interface ComponentCardProps {
  name: string;
  description: string;
  demo: React.ReactNode;
  code: string;
}

function ComponentCard({ name, description, demo, code }: ComponentCardProps) {
  const [showCode, setShowCode] = useState(false);

  return (
    <div 
      style={{
        borderBottom: '1px solid var(--border-secondary)',
        paddingBottom: 'var(--space-8)',
        marginBottom: 'var(--space-8)'
      }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', marginBottom: 'var(--space-6)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h3 style={{ 
              fontSize: 'var(--font-size-xl)',
              fontWeight: 'var(--font-weight-semibold)',
              color: 'var(--primary)',
              marginBottom: 'var(--space-2)'
            }}>
              {name}
            </h3>
            <p style={{ fontSize: 'var(--font-size-md)', color: 'var(--secondary)' }}>
              {description}
            </p>
          </div>
          <Button 
            variant="secondary"
            onClick={() => setShowCode(!showCode)}
          >
            {showCode ? 'Hide Code' : 'Show Code'}
          </Button>
        </div>
      </div>
      
      <div 
        style={{ 
          padding: 'var(--space-8)',
          backgroundColor: 'var(--bg-primary)',
          border: '1px solid var(--border-primary)',
          borderRadius: 'var(--radius-md)',
          minHeight: '120px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: showCode ? 'var(--space-4)' : 0
        }}
      >
        {demo}
      </div>
      
      {showCode && (
        <pre 
          className="overflow-x-auto"
          style={{
            padding: 'var(--space-4)',
            backgroundColor: 'var(--surface-alt)',
            borderRadius: 'var(--radius-md)',
            fontSize: 'var(--font-size-sm)',
            border: '1px solid var(--border-primary)',
            marginTop: 'var(--space-4)'
          }}
        >
          <code>{code}</code>
        </pre>
      )}
    </div>
  );
}

interface ColorSwatchProps {
  name: string;
  cssVar: string;
  description?: string;
}

function ColorSwatch({ name, cssVar, description }: ColorSwatchProps) {
  return (
    <div className="flex flex-col" style={{ gap: 'var(--space-2)' }}>
      <div 
        style={{ 
          height: '64px',
          backgroundColor: `var(${cssVar})`,
          border: '1px solid var(--border-primary)',
          borderRadius: 'var(--radius-md)'
        }}
      />
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
        <p style={{ fontWeight: 'var(--font-weight-medium)' }}>{name}</p>
        <code style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>{cssVar}</code>
        {description && (
          <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>{description}</p>
        )}
      </div>
    </div>
  );
}

function Separator() {
  return <div style={{ height: '1px', backgroundColor: 'var(--border-secondary)', width: '100%' }} />;
}

export default function ComponentGallery() {
  const [progressValue, setProgressValue] = useState(60);
  const [checkboxValue, setCheckboxValue] = useState(false);
  const [radioValue, setRadioValue] = useState('option1');
  const [switchValue, setSwitchValue] = useState(false);

  const colorTokens = {
    base: [
      { name: 'Primary', cssVar: '--primary', description: 'Main text, primary actions' },
      { name: 'Secondary', cssVar: '--secondary', description: 'Secondary text, muted content' },
      { name: 'Tertiary', cssVar: '--tertiary', description: 'Subtle text, disabled states' },
      { name: 'Border Primary', cssVar: '--border-primary', description: 'Primary borders, form elements' },
      { name: 'Border Secondary', cssVar: '--border-secondary', description: 'Dividers, subtle separators' },
      { name: 'BG Primary', cssVar: '--bg-primary', description: 'Cards, surfaces, main backgrounds' },
      { name: 'BG Secondary', cssVar: '--bg-secondary', description: 'Page backgrounds, subtle fills' },
    ],
    status: [
      { name: 'Critical', cssVar: '--critical', description: 'Error states' },
      { name: 'Critical Dark', cssVar: '--critical-dark', description: 'Error hover/active' },
      { name: 'Critical Light', cssVar: '--critical-light', description: 'Error backgrounds' },
      { name: 'Warning', cssVar: '--warning', description: 'Warning states' },
      { name: 'Warning Dark', cssVar: '--warning-dark', description: 'Warning hover/active' },
      { name: 'Warning Light', cssVar: '--warning-light', description: 'Warning backgrounds' },
      { name: 'Positive', cssVar: '--positive', description: 'Success states' },
      { name: 'Positive Dark', cssVar: '--positive-dark', description: 'Success hover/active' },
      { name: 'Positive Light', cssVar: '--positive-light', description: 'Success backgrounds' },
      { name: 'Neutral', cssVar: '--neutral', description: 'Info states' },
      { name: 'Neutral Dark', cssVar: '--neutral-dark', description: 'Info hover/active' },
      { name: 'Neutral Light', cssVar: '--neutral-light', description: 'Info backgrounds' },
    ],
    buttons: [
      { name: 'Primary BG', cssVar: '--button-primary-bg', description: 'Primary button background' },
      { name: 'Primary Text', cssVar: '--button-primary-text', description: 'Primary button text' },
      { name: 'Primary Hover', cssVar: '--button-primary-hover-bg', description: 'Primary button hover' },
      { name: 'Secondary BG', cssVar: '--button-secondary-bg', description: 'Secondary button background' },
      { name: 'Secondary Text', cssVar: '--button-secondary-text', description: 'Secondary button text' },
      { name: 'Secondary Hover', cssVar: '--button-secondary-hover-bg', description: 'Secondary button hover' },
      { name: 'Destructive BG', cssVar: '--button-destructive-bg', description: 'Destructive button background' },
      { name: 'Destructive Text', cssVar: '--button-destructive-text', description: 'Destructive button text' },
      { name: 'Destructive Hover', cssVar: '--button-destructive-hover-bg', description: 'Destructive button hover' },
    ],
    badges: [
      { name: 'Normal BG', cssVar: '--badge-normal-bg', description: 'Normal badge background' },
      { name: 'Normal Text', cssVar: '--badge-normal-text', description: 'Normal badge text' },
      { name: 'Danger BG', cssVar: '--badge-danger-bg', description: 'Danger badge background' },
      { name: 'Danger Text', cssVar: '--badge-danger-text', description: 'Danger badge text' },
      { name: 'Success BG', cssVar: '--badge-success-bg', description: 'Success badge background' },
      { name: 'Success Text', cssVar: '--badge-success-text', description: 'Success badge text' },
      { name: 'Warning BG', cssVar: '--badge-warning-bg', description: 'Warning badge background' },
      { name: 'Warning Text', cssVar: '--badge-warning-text', description: 'Warning badge text' },
      { name: 'Neutral BG', cssVar: '--badge-neutral-bg', description: 'Neutral badge background' },
      { name: 'Neutral Text', cssVar: '--badge-neutral-text', description: 'Neutral badge text' },
    ],
    forms: [
      { name: 'Surface', cssVar: '--surface', description: 'Form element backgrounds' },
      { name: 'Surface Alt', cssVar: '--surface-alt', description: 'Alternate surface background' },
      { name: 'Surface Hover', cssVar: '--surface-hover', description: 'Surface hover state' },
      { name: 'Input', cssVar: '--input', description: 'Input text color' },
      { name: 'Input Muted', cssVar: '--input-muted', description: 'Muted input text' },
      { name: 'Input Disabled', cssVar: '--input-disabled', description: 'Disabled input text' },
      { name: 'Placeholder', cssVar: '--placeholder', description: 'Placeholder text' },
      { name: 'Helper', cssVar: '--helper', description: 'Helper text' },
      { name: 'Border', cssVar: '--border', description: 'Form element borders' },
      { name: 'Border Hover', cssVar: '--border-hover', description: 'Border hover state' },
      { name: 'Focus', cssVar: '--focus', description: 'Focus border color' },
      { name: 'Focus Ring', cssVar: '--focus-ring', description: 'Focus ring color' },
    ]
  };

  const typographyScale = [
    { name: 'Extra Extra Large', cssVar: '--font-size-xxl', size: '28px', usage: 'Page titles, hero headings' },
    { name: 'Extra Large', cssVar: '--font-size-xl', size: '24px', usage: 'Section headings, h2' },
    { name: 'Large', cssVar: '--font-size-lg', size: '20px', usage: 'Subsection headings, h3' },
    { name: 'Medium', cssVar: '--font-size-md', size: '16px', usage: 'Body text, default size' },
    { name: 'Small', cssVar: '--font-size-sm', size: '14px', usage: 'Labels, captions' },
    { name: 'Extra Small', cssVar: '--font-size-xs', size: '12px', usage: 'Fine print, metadata' },
  ];

  const fontWeights = [
    { name: 'Regular', cssVar: '--font-weight-regular', value: '400', usage: 'Body text' },
    { name: 'Medium', cssVar: '--font-weight-medium', value: '500', usage: 'Emphasized text' },
    { name: 'Semibold', cssVar: '--font-weight-semibold', value: '600', usage: 'Headings, buttons' },
    { name: 'Bold', cssVar: '--font-weight-bold', value: '700', usage: 'Strong emphasis' },
  ];

  const lineHeights = [
    { name: 'Tight', cssVar: '--line-height-tight', value: '1.2', usage: 'Headings' },
    { name: 'Normal', cssVar: '--line-height-normal', value: '1.4', usage: 'Body text, UI elements' },
    { name: 'Relaxed', cssVar: '--line-height-relaxed', value: '1.6', usage: 'Long-form content' },
  ];

  const spacingScale = [
    { name: '0px', cssVar: '--space-0', value: '0px' },
    { name: '4px', cssVar: '--space-1', value: '4px' },
    { name: '8px', cssVar: '--space-2', value: '8px' },
    { name: '12px', cssVar: '--space-3', value: '12px' },
    { name: '16px', cssVar: '--space-4', value: '16px' },
    { name: '20px', cssVar: '--space-5', value: '20px' },
    { name: '24px', cssVar: '--space-6', value: '24px' },
    { name: '28px', cssVar: '--space-7', value: '28px' },
    { name: '32px', cssVar: '--space-8', value: '32px' },
    { name: '36px', cssVar: '--space-9', value: '36px' },
    { name: '40px', cssVar: '--space-10', value: '40px' },
    { name: '44px', cssVar: '--space-11', value: '44px' },
    { name: '48px', cssVar: '--space-12', value: '48px' },
    { name: '52px', cssVar: '--space-13', value: '52px' },
    { name: '56px', cssVar: '--space-14', value: '56px' },
    { name: '60px', cssVar: '--space-15', value: '60px' },
    { name: '64px', cssVar: '--space-16', value: '64px' },
    { name: '80px', cssVar: '--space-20', value: '80px' },
    { name: '96px', cssVar: '--space-24', value: '96px' },
  ];

  const radiusScale = [
    { name: 'None', cssVar: '--radius-none', value: '0px' },
    { name: 'Small', cssVar: '--radius-sm', value: '4px' },
    { name: 'Medium', cssVar: '--radius-md', value: '8px' },
    { name: 'Large', cssVar: '--radius-lg', value: '12px' },
    { name: 'Extra Large', cssVar: '--radius-xl', value: '16px' },
    { name: 'Full', cssVar: '--radius-full', value: '9999px' },
    { name: 'Circle', cssVar: '--radius-circle', value: '50%' },
  ];

  const shadowScale = [
    { name: 'Small', cssVar: '--shadow-sm', description: 'Subtle elevation' },
    { name: 'Medium', cssVar: '--shadow-md', description: 'Moderate elevation' },
    { name: 'Large', cssVar: '--shadow-lg', description: 'High elevation' },
    { name: 'Extra Large', cssVar: '--shadow-xl', description: 'Maximum elevation' },
  ];

  const componentTokens = {
    heights: [
      { name: 'Small', cssVar: '--component-height-sm', value: '36px' },
      { name: 'Medium', cssVar: '--component-height-md', value: '40px' },
      { name: 'Large', cssVar: '--component-height-lg', value: '52px' },
      { name: 'Extra Large', cssVar: '--component-height-xl', value: '64px' },
    ],
    fontSizes: [
      { name: 'Small', cssVar: '--component-font-size-sm', value: '14px' },
      { name: 'Medium', cssVar: '--component-font-size-md', value: '14px' },
      { name: 'Large', cssVar: '--component-font-size-lg', value: '16px' },
      { name: 'Extra Large', cssVar: '--component-font-size-xl', value: '18px' },
    ],
    gaps: [
      { name: 'Small', cssVar: '--component-gap-sm', value: '8px' },
      { name: 'Medium', cssVar: '--component-gap-md', value: '12px' },
      { name: 'Large', cssVar: '--component-gap-lg', value: '16px' },
    ]
  };

  // Table data with required 'id' property
  const tableData = [
    { id: 1, name: 'Item 1', status: 'Active' },
    { id: 2, name: 'Item 2', status: 'Pending' },
    { id: 3, name: 'Item 3', status: 'Completed' },
  ];

  const tableColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Status', dataIndex: 'status', key: 'status' },
  ];

  const components: ComponentCardProps[] = [
    {
      name: 'Button',
      description: 'Interactive button component with multiple variants',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)', width: '100%' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Filled Variants</p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
              <Button variant="primary">Primary</Button>
              <Button variant="secondary">Secondary</Button>
              <Button variant="destructive">Destructive</Button>
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Text & Link Variants</p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-4)', alignItems: 'center' }}>
              <Button variant="text" icon="add">Button</Button>
              <Button variant="link" icon="add">Button</Button>
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>With Icons</p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
              <Button variant="primary" icon="check">With Icon</Button>
              <Button variant="secondary" icon="check">Secondary Icon</Button>
              <Button variant="destructive" icon="delete">Delete</Button>
            </div>
          </div>
        </div>
      ),
      code: `{/* Filled Variants */}
<Button variant="primary">Primary</Button>
<Button variant="secondary">Secondary</Button>
<Button variant="destructive">Destructive</Button>

{/* Text & Link Variants (no background) */}
<Button variant="text" icon="add">Button</Button>
<Button variant="link" icon="add">Button</Button>

{/* With Icons */}
<Button variant="primary" icon="check">With Icon</Button>
<Button variant="secondary" icon="check">Secondary Icon</Button>`
    },
    {
      name: 'Input',
      description: 'Text input field with label, prefix, suffix, and clear button',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)', width: '100%', maxWidth: '400px' }}>
          <Input 
            label="Label"
            placeholder="Value"
            leadingIcon="rupee-coin"
          />
          <Input 
            label="Amount"
            placeholder="Enter amount"
            leadingIcon="rupee-coin"
          />
          <Input 
            label="Email"
            type="email"
            placeholder="Enter your email"
            leadingIcon="mail"
          />
          <Input 
            label="Disabled"
            placeholder="Disabled input"
            disabled
          />
          <Input 
            label="With Error"
            placeholder="Invalid value"
            error="This field is required"
            helperText="This field is required"
          />
        </div>
      ),
      code: `<Input 
  label="Label"
  placeholder="Value"
  leadingIcon="rupee-coin"
/>

<Input 
  label="Amount"
  placeholder="Enter amount"
  leadingIcon="rupee-coin"
/>

<Input 
  label="With Error"
  placeholder="Invalid value"
  error="This field is required"
  helperText="This field is required"
/>`
    },
    {
      name: 'Badge',
      description: 'Status indicator or label badge (non-interactive)',
      demo: (
        <div className="flex flex-wrap" style={{ gap: 'var(--space-2)' }}>
          <Badge variant="normal">Normal</Badge>
          <Badge variant="neutral">Neutral</Badge>
          <Badge variant="warning">Warning</Badge>
          <Badge variant="danger">Danger</Badge>
          <Badge variant="success">Success</Badge>
        </div>
      ),
      code: `<Badge variant="normal">Normal</Badge>
<Badge variant="neutral">Neutral</Badge>
<Badge variant="warning">Warning</Badge>
<Badge variant="danger">Danger</Badge>
<Badge variant="success">Success</Badge>`
    },
    {
      name: 'QuickFilters',
      description: 'Interactive filter chips for data filtering with single and multi-option support (clickable, with counts)',
      demo: (
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {/* Single Option Filters */}
          <div>
            <h3 style={{ fontSize: 'var(--font-size-sm)', marginBottom: 'var(--space-2)', color: 'var(--secondary)' }}>Single Option Filters</h3>
            <QuickFilters 
              filters={[
                { id: 'stoppage', label: 'Long Stoppage', count: 19, type: 'alert' },
                { id: 'deviation', label: 'Route Deviation', count: 19, type: 'alert' },
              ]}
              onFilterClick={(filterId, optionId) => console.log('Filter clicked:', filterId, optionId)}
              onFilterRemove={(filterId, optionId) => console.log('Filter removed:', filterId, optionId)}
            />
          </div>
          
          {/* Multi-Option Filters */}
          <div>
            <h3 style={{ fontSize: 'var(--font-size-sm)', marginBottom: 'var(--space-2)', color: 'var(--secondary)' }}>Multi-Option Filters</h3>
            <QuickFilters 
              filters={[
                {
                  id: 'delayed',
                  label: 'Delayed',
                  count: 51,
                  type: 'normal',
                  options: [
                    { id: '0-6hrs', label: '0-6 hrs', count: 28, type: 'warning' },
                    { id: '6-12hrs', label: '6-12 hrs', count: 18, type: 'warning' },
                    { id: '12plus', label: '12+ hrs', count: 5, type: 'alert' }
                  ]
                },
                {
                  id: 'eway',
                  label: 'E Way bill',
                  type: 'normal',
                  options: [
                    { id: 'expiring', label: 'Expiring in 3 hrs', count: 28, type: 'warning' },
                    { id: 'expired', label: 'Expired', count: 18, type: 'alert' }
                  ]
                },
                {
                  id: 'eta',
                  label: 'ETA',
                  type: 'normal',
                  options: [
                    { id: '6hrs', label: '6 hrs', count: 28, type: 'success' },
                    { id: '12hrs', label: '12 hrs', count: 18, type: 'success' },
                    { id: '24plus', label: '24+ hrs', count: 5, type: 'alert' }
                  ]
                }
              ]}
              onFilterClick={(filterId, optionId) => console.log('Filter clicked:', filterId, optionId)}
              onFilterRemove={(filterId, optionId) => console.log('Filter removed:', filterId, optionId)}
            />
          </div>
        </div>
      ),
      code: `// Single Option Filters
<QuickFilters 
  filters={[
    { id: 'stoppage', label: 'Long Stoppage', count: 19, type: 'alert' },
    { id: 'deviation', label: 'Route Deviation', count: 19, type: 'alert' },
  ]}
  onFilterClick={(filterId, optionId) => handleClick(filterId, optionId)}
  onFilterRemove={(filterId, optionId) => handleRemove(filterId, optionId)}
/>

// Multi-Option Filters
<QuickFilters 
  filters={[
    {
      id: 'delayed',
      label: 'Delayed',
      count: 51,
      type: 'normal',
      options: [
        { id: '0-6hrs', label: '0-6 hrs', count: 28, type: 'warning' },
        { id: '6-12hrs', label: '6-12 hrs', count: 18, type: 'warning' },
        { id: '12plus', label: '12+ hrs', count: 5, type: 'alert' }
      ]
    },
    {
      id: 'eway',
      label: 'E Way bill',
      type: 'normal',
      options: [
        { id: 'expiring', label: 'Expiring in 3 hrs', count: 28, type: 'warning' },
        { id: 'expired', label: 'Expired', count: 18, type: 'alert' }
      ]
    }
  ]}
  onFilterClick={(filterId, optionId) => handleClick(filterId, optionId)}
  onFilterRemove={(filterId, optionId) => handleRemove(filterId, optionId)}
/>`
    },
    {
      name: 'Table',
      description: 'Data table with sortable columns',
      demo: (
        <div style={{ width: '100%' }}>
          <Table 
            columns={tableColumns}
            data={tableData}
            className="bg-[var(--bg-primary)]"
          />
        </div>
      ),
      code: `const columns = [
  { title: 'Name', dataIndex: 'name', key: 'name' },
  { title: 'Status', dataIndex: 'status', key: 'status' }
];

const data = [
  { id: 1, name: 'Item 1', status: 'Active' },
  { id: 2, name: 'Item 2', status: 'Pending' }
];

<Table columns={columns} data={data} />`
    },
    {
      name: 'Tabs',
      description: 'Tab component from FT Design System with badges and icons',
      demo: (
        <div style={{ width: '100%' }}>
          <Tabs 
            tabs={[
              { label: 'Planned', badge: true, badgeCount: 56, icon: true },
              { label: 'In Transit', badge: true, badgeCount: 24, icon: true },
              { label: 'Delivered', badge: true, badgeCount: 128, icon: true },
              { label: 'Completed' }
            ]}
            activeTab={0}
            className="bg-[var(--bg-primary)]"
          />
        </div>
      ),
      code: `<Tabs 
  tabs={[
    { label: 'Planned', badge: true, badgeCount: 56, icon: true },
    { label: 'In Transit', badge: true, badgeCount: 24, icon: true },
    { label: 'Delivered', badge: true, badgeCount: 128, icon: true }
  ]}
  activeTab={0}
/>`
    },
    {
      name: 'ProgressBar',
      description: 'Visual progress indicator',
      demo: (
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <ProgressBar value={progressValue} />
          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            <Button variant="secondary" onClick={() => setProgressValue(Math.max(0, progressValue - 10))}>-10%</Button>
            <Button variant="secondary" onClick={() => setProgressValue(Math.min(100, progressValue + 10))}>+10%</Button>
          </div>
        </div>
      ),
      code: `const [progress, setProgress] = useState(60);

<ProgressBar value={progress} />`
    },
    {
      name: 'Checkbox',
      description: 'Toggle checkbox for binary choices',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <Checkbox 
              checked={checkboxValue}
              onChange={(e) => setCheckboxValue(e.target.checked)}
              id="checkbox-demo"
            />
            <label htmlFor="checkbox-demo" style={{ cursor: 'pointer' }}>
              I agree to the terms
            </label>
          </div>
        </div>
      ),
      code: `const [checked, setChecked] = useState(false);

<Checkbox 
  checked={checked}
  onChange={(e) => setChecked(e.target.checked)}
/>`
    },
    {
      name: 'RadioGroup',
      description: 'Select one option from multiple choices',
      demo: (
        <RadioGroup 
          value={radioValue}
          onChange={(value) => setRadioValue(value)}
          options={[
            { value: 'option1', label: 'Option 1' },
            { value: 'option2', label: 'Option 2' },
            { value: 'option3', label: 'Option 3' },
          ]}
        />
      ),
      code: `const [value, setValue] = useState('option1');

<RadioGroup 
  value={value}
  onChange={setValue}
  options={[
    { value: 'option1', label: 'Option 1' },
    { value: 'option2', label: 'Option 2' }
  ]}
/>`
    },
    {
      name: 'Switch',
      description: 'Toggle switch for on/off states',
      demo: (
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <Switch 
            checked={switchValue}
            onChange={(e) => setSwitchValue(e.target.checked)}
            id="switch-demo"
          />
          <label htmlFor="switch-demo" style={{ cursor: 'pointer' }}>
            Enable notifications
          </label>
        </div>
      ),
      code: `const [enabled, setEnabled] = useState(false);

<Switch 
  checked={enabled}
  onChange={(e) => setEnabled(e.target.checked)}
/>`
    },
    {
      name: 'Dropdown',
      description: 'Select dropdown with search and segmentation support',
      demo: (
        <div style={{ width: '100%', maxWidth: '400px' }}>
          <Dropdown 
            label="Select Option"
            options={[
              { value: 'option1', label: 'Option 1' },
              { value: 'option2', label: 'Option 2' },
              { value: 'option3', label: 'Option 3' },
              { value: 'option4', label: 'Option 4' },
            ]}
            placeholder="Choose an option"
          />
        </div>
      ),
      code: `<Dropdown 
  label="Select Option"
  options={[
    { value: 'option1', label: 'Option 1' },
    { value: 'option2', label: 'Option 2' },
    { value: 'option3', label: 'Option 3' },
  ]}
  placeholder="Choose an option"
/>`
    },
    {
      name: 'DatePicker',
      description: 'Date and time selection component with range support',
      demo: (
        <div style={{ width: '100%', maxWidth: '400px' }}>
          <DatePicker 
            label="Select Date"
            placeholder="Choose a date"
          />
        </div>
      ),
      code: `<DatePicker 
  label="Select Date"
  placeholder="Choose a date"
/>`
    },
    {
      name: 'Statistic',
      description: 'Display numeric data with labels',
      demo: (
        <div style={{ display: 'flex', gap: 'var(--space-6)', flexWrap: 'wrap' }}>
          <Statistic label="Total Orders" value="1,234" labelPlacement="Top" />
          <Statistic label="Revenue" value="$45.2K" labelPlacement="Below" />
          <Statistic label="Active Users" value="892" labelPlacement="Top" />
        </div>
      ),
      code: `<Statistic label="Total Orders" value="1,234" labelPlacement="Top" />
<Statistic label="Revenue" value="$45.2K" labelPlacement="Below" />`
    },
    {
      name: 'Text',
      description: 'Typography text component with various sizes',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          <Text size="xs">Extra Small Text</Text>
          <Text size="sm">Small Text</Text>
          <Text size="md">Medium Text (Default)</Text>
          <Text size="lg">Large Text</Text>
          <Text size="xl">Extra Large Text</Text>
        </div>
      ),
      code: `<Text size="xs">Extra Small Text</Text>
<Text size="sm">Small Text</Text>
<Text size="md">Medium Text</Text>
<Text size="lg">Large Text</Text>`
    },
    {
      name: 'SubText',
      description: 'Secondary text component for captions and metadata',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
          <SubText icon="No">This is secondary text without icon</SubText>
          <SubText icon="Yes">This is secondary text with check icon</SubText>
        </div>
      ),
      code: `<SubText icon="No">Secondary text without icon</SubText>
<SubText icon="Yes">Secondary text with check icon</SubText>`
    },
    {
      name: 'Card',
      description: 'Container card component with eyebrow and footer sections',
      demo: (
        <div style={{ width: '100%', maxWidth: '400px' }}>
          <Card 
            content="Advanced"
            showEyebrow={true}
            showFooter={true}
          />
        </div>
      ),
      code: `<Card 
  content="Advanced"
  showEyebrow={true}
  showFooter={true}
/>`
    },
    {
      name: 'DisplayBlock',
      description: 'Display blocks for data visualization',
      demo: (
        <DisplayBlock 
          layout="Horizontal"
          blocks="3"
          padding="True"
        />
      ),
      code: `<DisplayBlock 
  layout="Horizontal"
  blocks="3"
  padding="True"
/>`
    },
    {
      name: 'NavigationMenu',
      description: 'Side navigation menu with footer actions',
      demo: (
        <div style={{ width: '100%', maxWidth: '300px', height: '400px' }}>
          <NavigationMenu 
            onNavigate={(item) => console.log('Navigate to:', item)}
            onClose={() => console.log('Close menu')}
          />
        </div>
      ),
      code: `<NavigationMenu 
  onNavigate={(item) => console.log('Navigate:', item)}
  onClose={() => console.log('Close')}
/>`
    },
    {
      name: 'AppHeader',
      description: 'Application header with user profile and notifications',
      demo: (
        <div style={{ width: '100%' }}>
          <AppHeader 
            size="Default"
            device="Desktop"
            user={{
              name: 'John Doe',
              role: 'Administrator',
              location: 'Mumbai, India'
            }}
            userCompany={{
              name: 'ft',
              displayName: 'Freight Tiger'
            }}
          />
        </div>
      ),
      code: `<AppHeader 
  size="Default"
  device="Desktop"
  user={{
    name: 'John Doe',
    role: 'Administrator',
    location: 'Mumbai'
  }}
/>`
    },
    {
      name: 'Footer',
      description: 'Page footer with action buttons',
      demo: (
        <Footer 
          buttonCount={3}
          leftSideButton={true}
          buttonTexts={['Cancel', 'Save Draft', 'Submit']}
          buttonVariants={['text', 'secondary', 'primary']}
        />
      ),
      code: `<Footer 
  buttonCount={3}
  leftSideButton={true}
  buttonTexts={['Cancel', 'Save Draft', 'Submit']}
  buttonVariants={['text', 'secondary', 'primary']}
/>`
    },
    {
      name: 'UserProfile',
      description: 'User profile dropdown with company info',
      demo: (
        <UserProfile 
          userName="John Doe"
          userRole="Administrator"
          userLocation="Mumbai, India"
          company={{
            name: 'ft',
            displayName: 'Freight Tiger'
          }}
        />
      ),
      code: `<UserProfile 
  userName="John Doe"
  userRole="Administrator"
  userLocation="Mumbai, India"
  company={{ name: 'ft' }}
/>`
    },
    {
      name: 'Collapsible',
      description: 'Expandable collapsible section with badges',
      demo: (
        <div style={{ width: '100%' }}>
          <Collapsible 
            header="Shipment Details"
            badges={{ loads: 5, invoices: 3 }}
            isExpanded={false}
          >
            <p style={{ padding: 'var(--space-4)', color: 'var(--secondary)' }}>
              Collapsible content goes here
            </p>
          </Collapsible>
        </div>
      ),
      code: `<Collapsible 
  header="Shipment Details"
  badges={{ loads: 5, invoices: 3 }}
  isExpanded={false}
>
  <p>Content here</p>
</Collapsible>`
    },
    {
      name: 'Steps',
      description: 'Step progress indicator for multi-step processes',
      demo: (
        <div style={{ width: '100%' }}>
          <Steps 
            steps={[
              { label: 'Details', completed: true },
              { label: 'Review', completed: false },
              { label: 'Payment', completed: false },
              { label: 'Confirm', completed: false },
            ]}
            currentStep={1}
            device="desktop"
          />
        </div>
      ),
      code: `<Steps 
  steps={[
    { label: 'Details', completed: true },
    { label: 'Review', completed: false },
    { label: 'Payment', completed: false },
  ]}
  currentStep={1}
/>`
    },
    {
      name: 'RadioSelector',
      description: 'Radio cards with headers and descriptions',
      demo: (
        <div style={{ width: '100%' }}>
          <RadioSelector 
            name="payment-method"
            defaultValue="card"
            options={[
              {
                value: 'card',
                header: 'Credit Card',
                description: 'Pay with credit or debit card'
              },
              {
                value: 'bank',
                header: 'Bank Transfer',
                description: 'Direct bank transfer'
              },
              {
                value: 'wallet',
                header: 'Digital Wallet',
                description: 'Use UPI or wallet'
              },
            ]}
          />
        </div>
      ),
      code: `<RadioSelector 
  name="payment-method"
  options={[
    {
      value: 'card',
      header: 'Credit Card',
      description: 'Pay with card'
    }
  ]}
/>`
    },
    {
      name: 'SegmentedTabs',
      description: 'Segmented control tabs',
      demo: (
        <SegmentedTabs 
          items={[
            { label: 'Day', value: 'day' },
            { label: 'Week', value: 'week' },
            { label: 'Month', value: 'month' },
            { label: 'Year', value: 'year' },
          ]}
          defaultValue="week"
        />
      ),
      code: `<SegmentedTabs 
  items={[
    { label: 'Day', value: 'day' },
    { label: 'Week', value: 'week' },
    { label: 'Month', value: 'month' },
  ]}
  defaultValue="week"
/>`
    },
    {
      name: 'ButtonGroup',
      description: 'Group of buttons displayed together with consistent styling',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', width: '100%' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Default Button Group</p>
            <ButtonGroup 
              buttons={[
                { id: '1', label: 'Save', variant: 'primary' },
                { id: '2', label: 'Cancel', variant: 'secondary' },
                { id: '3', label: 'Delete', variant: 'destructive' },
              ]}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Equal Width Buttons</p>
            <ButtonGroup 
              buttons={[
                { id: '4', label: 'Option 1', variant: 'secondary' },
                { id: '5', label: 'Option 2', variant: 'secondary' },
                { id: '6', label: 'Option 3', variant: 'secondary' },
              ]}
              equalWidth={true}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>With Icons</p>
            <ButtonGroup 
              buttons={[
                { id: '7', label: 'Add', variant: 'primary', icon: 'add' },
                { id: '8', label: 'Edit', variant: 'secondary', icon: 'edit' },
                { id: '9', label: 'Delete', variant: 'destructive', icon: 'delete' },
              ]}
            />
          </div>
        </div>
      ),
      code: `<ButtonGroup 
  buttons={[
    { id: '1', label: 'Save', variant: 'primary' },
    { id: '2', label: 'Cancel', variant: 'secondary' },
    { id: '3', label: 'Delete', variant: 'destructive' },
  ]}
/>

{/* Equal width buttons */}
<ButtonGroup 
  buttons={[
    { id: '4', label: 'Option 1', variant: 'secondary' },
    { id: '5', label: 'Option 2', variant: 'secondary' },
  ]}
  equalWidth={true}
/>

{/* With icons */}
<ButtonGroup 
  buttons={[
    { id: '6', label: 'Add', variant: 'primary', icon: 'add' },
    { id: '7', label: 'Edit', variant: 'secondary', icon: 'edit' },
  ]}
/>`
    },
    {
      name: 'UploadZone',
      description: 'File upload drop zone',
      demo: (
        <div style={{ width: '100%' }}>
          <UploadZone 
            onFileSelect={(files) => console.log('Files:', files)}
            acceptedFileTypes={['.pdf', '.xlsx', '.csv']}
            multiple={true}
          />
        </div>
      ),
      code: `<UploadZone 
  onFileSelect={(files) => console.log(files)}
  acceptedFileTypes={['.pdf', '.xlsx']}
  multiple={true}
/>`
    },
    {
      name: 'FileCard',
      description: 'File upload card with status and actions',
      demo: (
        <div style={{ width: '100%' }}>
          <FileCard 
            fileName="invoice-data.xlsx"
            fileType="xlsx"
            fileDate="2 mins ago"
            status="processed"
            stats={{ total: 150, success: 148, invalid: 2 }}
            variant="with-stats"
          />
        </div>
      ),
      code: `<FileCard 
  fileName="invoice.xlsx"
  fileType="xlsx"
  status="processed"
  stats={{ total: 150, success: 148, invalid: 2 }}
/>`
    },
    {
      name: 'FileThumbnail',
      description: 'File thumbnail with download action',
      demo: (
        <FileThumbnail 
          fileName="document.pdf"
          variant="uploaded"
          onDownload={() => console.log('Download')}
        />
      ),
      code: `<FileThumbnail 
  fileName="document.pdf"
  variant="uploaded"
  onDownload={() => console.log('Download')}
/>`
    },
    {
      name: 'FileTypeIcon',
      description: 'File type icon indicator',
      demo: (
        <div style={{ display: 'flex', gap: 'var(--space-4)' }}>
          <FileTypeIcon fileType="pdf" size="sm" />
          <FileTypeIcon fileType="xlsx" size="md" />
          <FileTypeIcon fileType="csv" size="lg" />
          <FileTypeIcon fileType="doc" size="md" variant="error" />
        </div>
      ),
      code: `<FileTypeIcon fileType="pdf" size="sm" />
<FileTypeIcon fileType="xlsx" size="md" />
<FileTypeIcon fileType="csv" size="lg" />`
    },
    {
      name: 'Typography',
      description: 'Typography component for text formatting',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          <Typography>Default typography text</Typography>
          <Typography>Formatted text component</Typography>
        </div>
      ),
      code: `<Typography>Default typography text</Typography>`
    },
    {
      name: 'Spacer',
      description: 'Spacing component for consistent vertical or horizontal spacing',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', width: '100%' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Vertical Spacers</p>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x2" />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x4" />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x6" />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Horizontal Spacers</p>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x2" horizontal />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x4" horizontal />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
              <Spacer size="x6" horizontal />
              <div style={{ width: '40px', height: '40px', backgroundColor: 'var(--primary)', borderRadius: 'var(--radius-sm)' }} />
            </div>
          </div>
        </div>
      ),
      code: `{/* Vertical spacer */}
<Spacer size="x2" />
<Spacer size="x4" />
<Spacer size="x6" />

{/* Horizontal spacer */}
<Spacer size="x2" horizontal />
<Spacer size="x4" horizontal />`
    },
    {
      name: 'Divider',
      description: 'Visual divider component for separating content sections',
      demo: (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)', width: '100%' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Primary Divider</p>
            <Divider type="primary" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Secondary Divider</p>
            <Divider type="secondary" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Tertiary Divider</p>
            <Divider type="tertiary" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', fontWeight: 'var(--font-weight-medium)' }}>Divider with Label</p>
            <Divider type="with-label" label="Section Title" />
          </div>
        </div>
      ),
      code: `<Divider type="primary" />
<Divider type="secondary" />
<Divider type="tertiary" />
<Divider type="with-label" label="Section Title" />`
    },
    {
      name: 'UserProfileDropdown',
      description: 'User profile dropdown menu component',
      demo: (
        <div style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
          <UserProfileDropdown 
            userName="John Doe"
            userRole="Administrator"
            userLocation="Mumbai, India"
            company={{
              name: 'ft',
              displayName: 'Freight Tiger'
            }}
          />
        </div>
      ),
      code: `<UserProfileDropdown 
  userName="John Doe"
  userRole="Administrator"
  userLocation="Mumbai, India"
  company={{ name: 'ft', displayName: 'Freight Tiger' }}
/>`
    },
    {
      name: 'ListingLayout',
      description: 'Template layout component for listing pages',
      demo: (
        <div style={{ width: '100%' }}>
          <ListingLayout 
            variant="default"
            layout="single-column"
            sections={[
              {
                title: 'Section 1',
                content: 'Content for section 1'
              }
            ]}
          />
        </div>
      ),
      code: `<ListingLayout 
  variant="default"
  layout="single-column"
  sections={[
    { title: 'Section 1', content: 'Content here' }
  ]}
/>`
    },
    {
      name: 'Icons (200+ Available)',
      description: 'Complete icon library from ft-design-system',
      demo: (
        <div style={{ width: '100%' }}>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))', 
            gap: 'var(--space-3)',
            maxHeight: '400px',
            overflow: 'auto',
            padding: 'var(--space-2)',
            backgroundColor: 'var(--surface-alt)',
            borderRadius: 'var(--radius-md)'
          }}>
            {[
              'add', 'check', 'close', 'delete', 'edit', 'download', 'upload',
              'search', 'filter', 'calendar', 'clock', 'bell', 'user', 'settings',
              'arrow-up', 'arrow-down', 'arrow-left', 'arrow-right',
              'chevron-up', 'chevron-down', 'chevron-left', 'chevron-right',
              'mail', 'phone', 'location', 'home', 'dashboard', 'reports',
              'truck', 'warehouse', 'vehicle', 'tracker', 'gps', 'map',
              'rupee-coin', 'document', 'file', 'excel', 'save', 'copy',
              'play', 'preview', 'refresh', 'share', 'star', 'success'
            ].map((iconName) => (
              <div 
                key={iconName} 
                style={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center', 
                  gap: 'var(--space-1)',
                  padding: 'var(--space-2)',
                  backgroundColor: 'var(--surface)',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-primary)'
                }}
              >
                <Button variant="secondary" icon={iconName} style={{ minWidth: '40px' }} />
                <span style={{ 
                  fontSize: 'var(--font-size-xs)', 
                  color: 'var(--secondary)',
                  textAlign: 'center',
                  wordBreak: 'break-word'
                }}>
                  {iconName}
                </span>
              </div>
            ))}
          </div>
          <p style={{ 
            marginTop: 'var(--space-4)', 
            fontSize: 'var(--font-size-sm)', 
            color: 'var(--secondary)',
            textAlign: 'center'
          }}>
            Showing 48 of 200+ available icons. Import from ft-design-system/ai
          </p>
        </div>
      ),
      code: `// Use icons with Button component
<Button variant="primary" icon="add">Add Item</Button>
<Button variant="secondary" icon="check">Confirm</Button>
<Button variant="text" icon="download">Download</Button>
<Button variant="link" icon="arrow-right">Next</Button>

// Available icons include:
// add, check, close, delete, edit, download, upload,
// search, filter, calendar, clock, bell, user, settings,
// arrow-*, chevron-*, mail, phone, location, home,
// dashboard, reports, truck, warehouse, vehicle, tracker,
// gps, rupee-coin, document, file, excel, save, copy,
// and 200+ more...`
    },
    {
      name: 'Breadcrumb (Example)',
      description: 'Navigation breadcrumb - Not available in package',
      demo: (
        <MissingComponent 
          name="Breadcrumb"
          description="Component for showing navigation path"
        />
      ),
      code: `// Component not available in ft-design-system/ai
<MissingComponent 
  name="Breadcrumb"
  description="Component for showing navigation path"
/>`
    },
  ];

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-secondary)' }}>
      
      {/* Header */}
      <div 
        className="border-b"
        style={{ 
          borderColor: 'var(--border-primary)',
          backgroundColor: 'var(--bg-primary)'
        }}
      >
        <div style={{ 
          maxWidth: '1440px',
          margin: '0 auto',
          padding: 'var(--space-6) var(--space-5)'
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <h1 style={{ 
              fontSize: 'var(--font-size-xxl)',
              fontWeight: 'var(--font-weight-semibold)',
              color: 'var(--primary)'
            }}>
              FT Design System
            </h1>
            <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
              Complete gallery of Freight Tiger Design System tokens and components with multi-theme support
            </p>
          </div>
        </div>
      </div>

      {/* Design Tokens Section */}
      <div style={{ 
        maxWidth: '1440px',
        margin: '0 auto',
        padding: 'var(--space-8) var(--space-5)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-12)' }}>
          
          {/* Colors Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Color System
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                Semantic color palette with multi-theme support (Light, Dark, Night)
              </p>
            </div>

            {/* Base Colors */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Base Colors
              </h3>
              <div style={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 'var(--space-4)'
              }}>
                {colorTokens.base.map((color) => (
                  <ColorSwatch key={color.cssVar} {...color} />
                ))}
              </div>
            </div>

            {/* Status Colors */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Status Colors
              </h3>
              <div style={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 'var(--space-4)'
              }}>
                {colorTokens.status.map((color) => (
                  <ColorSwatch key={color.cssVar} {...color} />
                ))}
              </div>
            </div>

            {/* Button Colors */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Button System Colors
              </h3>
              <div style={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 'var(--space-4)'
              }}>
                {colorTokens.buttons.map((color) => (
                  <ColorSwatch key={color.cssVar} {...color} />
                ))}
              </div>
            </div>

            {/* Badge Colors */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Badge System Colors
              </h3>
              <div style={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 'var(--space-4)'
              }}>
                {colorTokens.badges.map((color) => (
                  <ColorSwatch key={color.cssVar} {...color} />
                ))}
              </div>
            </div>

            {/* Form Colors */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Form System Colors
              </h3>
              <div style={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 'var(--space-4)'
              }}>
                {colorTokens.forms.map((color) => (
                  <ColorSwatch key={color.cssVar} {...color} />
                ))}
              </div>
            </div>
          </section>

          <Separator />

          {/* Typography Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Typography System
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                Type scale using Inter font family with responsive sizing
              </p>
            </div>

            {/* Font Sizes */}
            <div style={{
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)'
            }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                marginBottom: 'var(--space-2)'
              }}>
                Font Sizes
              </h3>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', marginBottom: 'var(--space-6)' }}>
                Responsive font size scale
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
                {typographyScale.map((type, idx) => (
                  <div 
                    key={type.cssVar} 
                    style={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      gap: 'var(--space-2)',
                      paddingBottom: 'var(--space-6)',
                      borderBottom: idx < typographyScale.length - 1 ? '1px solid var(--border-primary)' : 'none'
                    }}
                  >
                    <div style={{ fontSize: `var(${type.cssVar})`, color: 'var(--primary)' }}>
                      The quick brown fox jumps over the lazy dog
                    </div>
                    <div style={{ 
                      display: 'flex', 
                      gap: 'var(--space-4)', 
                      flexWrap: 'wrap',
                      fontSize: 'var(--font-size-sm)',
                      color: 'var(--secondary)'
                    }}>
                      <span>{type.name}</span>
                      <span>•</span>
                      <code>{type.cssVar}</code>
                      <span>•</span>
                      <span>{type.size}</span>
                      <span>•</span>
                      <span>{type.usage}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Font Weights */}
            <div style={{
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)'
            }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                marginBottom: 'var(--space-2)'
              }}>
                Font Weights
              </h3>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', marginBottom: 'var(--space-6)' }}>
                Weight variations for different emphasis levels
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {fontWeights.map((weight) => (
                  <div 
                    key={weight.cssVar}
                    style={{ 
                      display: 'flex', 
                      alignItems: 'center',
                      gap: 'var(--space-4)'
                    }}
                  >
                    <div style={{ width: '120px', fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                      {weight.name}
                    </div>
                    <code style={{ width: '180px', fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {weight.cssVar}
                    </code>
                    <div 
                      style={{ 
                        flex: 1,
                        fontWeight: `var(${weight.cssVar})`,
                        fontSize: 'var(--font-size-md)'
                      }}
                    >
                      The quick brown fox ({weight.value})
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Line Heights */}
            <div style={{
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)'
            }}>
              <h3 style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                marginBottom: 'var(--space-2)'
              }}>
                Line Heights
              </h3>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', marginBottom: 'var(--space-6)' }}>
                Line height scale for different content types
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {lineHeights.map((lh) => (
                  <div 
                    key={lh.cssVar}
                    style={{ 
                      display: 'flex', 
                      flexDirection: 'column',
                      gap: 'var(--space-2)'
                    }}
                  >
                    <div style={{ 
                      display: 'flex',
                      gap: 'var(--space-4)',
                      fontSize: 'var(--font-size-sm)',
                      color: 'var(--secondary)'
                    }}>
                      <span>{lh.name}</span>
                      <span>•</span>
                      <code>{lh.cssVar}</code>
                      <span>•</span>
                      <span>{lh.value}</span>
                      <span>•</span>
                      <span>{lh.usage}</span>
                    </div>
                    <div style={{ lineHeight: `var(${lh.cssVar})` }}>
                      This is sample text demonstrating the line height. Multiple lines help visualize spacing between lines of text.
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </section>

          <Separator />

          {/* Spacing Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Spacing Scale
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                8-point grid spacing system (4px increments)
              </p>
            </div>

            <div style={{
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {spacingScale.map((space) => (
                  <div 
                    key={space.cssVar} 
                    style={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: 'var(--space-4)' 
                    }}
                  >
                    <div style={{ 
                      width: '60px', 
                      fontSize: 'var(--font-size-sm)', 
                      color: 'var(--secondary)' 
                    }}>
                      {space.name}
                    </div>
                    <code style={{ 
                      width: '100px', 
                      fontSize: 'var(--font-size-sm)', 
                      color: 'var(--secondary)' 
                    }}>
                      {space.cssVar}
                    </code>
                    <div 
                      style={{ 
                        height: '32px', 
                        width: `var(${space.cssVar})`,
                        backgroundColor: 'var(--neutral)',
                        borderRadius: 'var(--radius-sm)'
                      }}
                    />
                  </div>
                ))}
              </div>
            </div>
          </section>

          <Separator />

          {/* Border Radius Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Border Radius
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                Consistent rounded corner values for UI elements
              </p>
            </div>

            <div style={{ 
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
              gap: 'var(--space-4)'
            }}>
              {radiusScale.map((radius) => (
                <div 
                  key={radius.cssVar}
                  style={{
                    backgroundColor: 'var(--bg-primary)',
                    border: '1px solid var(--border-primary)',
                    borderRadius: 'var(--radius-md)',
                    padding: 'var(--space-6)'
                  }}
                >
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                    <div 
                      style={{ 
                        height: '96px',
                        backgroundColor: 'var(--primary)',
                        borderRadius: `var(${radius.cssVar})`
                      }}
                    />
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
                      <p style={{ fontWeight: 'var(--font-weight-medium)' }}>{radius.name}</p>
                      <code style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                        {radius.cssVar}
                      </code>
                      <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                        {radius.value}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <Separator />

          {/* Shadows Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Shadows
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                Elevation scale for creating depth and hierarchy
              </p>
            </div>

            <div style={{ 
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
              gap: 'var(--space-4)'
            }}>
              {shadowScale.map((shadow) => (
                <div 
                  key={shadow.cssVar}
                  style={{
                    padding: 'var(--space-6)',
                    backgroundColor: 'var(--bg-primary)',
                    borderRadius: 'var(--radius-md)',
                    boxShadow: `var(${shadow.cssVar})`
                  }}
                >
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    <p style={{ fontWeight: 'var(--font-weight-medium)' }}>{shadow.name}</p>
                    <code style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {shadow.cssVar}
                    </code>
                    <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {shadow.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <Separator />

          {/* Component Tokens Section */}
          <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                Component Tokens
              </h2>
              <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
                Specific design tokens for UI component sizing and spacing
              </p>
            </div>

            <div style={{ 
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
              gap: 'var(--space-4)'
            }}>
              {/* Component Heights */}
              <div style={{
                backgroundColor: 'var(--bg-primary)',
                border: '1px solid var(--border-primary)',
                borderRadius: 'var(--radius-md)',
                padding: 'var(--space-6)'
              }}>
                <h3 style={{ 
                  fontSize: 'var(--font-size-lg)',
                  fontWeight: 'var(--font-weight-semibold)',
                  marginBottom: 'var(--space-6)'
                }}>
                  Component Heights
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  {componentTokens.heights.map((height) => (
                    <div key={height.cssVar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
                      <div style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between',
                        fontSize: 'var(--font-size-sm)',
                        color: 'var(--secondary)'
                      }}>
                        <span>{height.name}</span>
                        <code>{height.value}</code>
                      </div>
                      <div 
                        style={{ 
                          height: `var(${height.cssVar})`,
                          backgroundColor: 'var(--surface-alt)',
                          borderRadius: 'var(--radius-md)',
                          border: '1px solid var(--border-primary)'
                        }}
                      />
                    </div>
                  ))}
                </div>
              </div>

              {/* Component Font Sizes */}
              <div style={{
                backgroundColor: 'var(--bg-primary)',
                border: '1px solid var(--border-primary)',
                borderRadius: 'var(--radius-md)',
                padding: 'var(--space-6)'
              }}>
                <h3 style={{ 
                  fontSize: 'var(--font-size-lg)',
                  fontWeight: 'var(--font-weight-semibold)',
                  marginBottom: 'var(--space-6)'
                }}>
                  Component Font Sizes
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  {componentTokens.fontSizes.map((fontSize) => (
                    <div key={fontSize.cssVar} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
                      <div style={{ width: '100px', fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                        {fontSize.name}
                      </div>
                      <div style={{ fontSize: `var(${fontSize.cssVar})` }}>
                        Sample Text
                      </div>
                      <code style={{ marginLeft: 'auto', fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                        {fontSize.value}
                      </code>
                    </div>
                  ))}
                </div>
              </div>

              {/* Component Gaps */}
              <div style={{
                backgroundColor: 'var(--bg-primary)',
                border: '1px solid var(--border-primary)',
                borderRadius: 'var(--radius-md)',
                padding: 'var(--space-6)'
              }}>
                <h3 style={{ 
                  fontSize: 'var(--font-size-lg)',
                  fontWeight: 'var(--font-weight-semibold)',
                  marginBottom: 'var(--space-6)'
                }}>
                  Component Gaps
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  {componentTokens.gaps.map((gap) => (
                    <div key={gap.cssVar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
                      <div style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between',
                        fontSize: 'var(--font-size-sm)',
                        color: 'var(--secondary)'
                      }}>
                        <span>{gap.name}</span>
                        <code>{gap.value}</code>
                      </div>
                      <div style={{ display: 'flex', gap: `var(${gap.cssVar})` }}>
                        <div style={{ 
                          width: '40px', 
                          height: '40px', 
                          backgroundColor: 'var(--neutral)',
                          borderRadius: 'var(--radius-sm)'
                        }} />
                        <div style={{ 
                          width: '40px', 
                          height: '40px', 
                          backgroundColor: 'var(--neutral)',
                          borderRadius: 'var(--radius-sm)'
                        }} />
                        <div style={{ 
                          width: '40px', 
                          height: '40px', 
                          backgroundColor: 'var(--neutral)',
                          borderRadius: 'var(--radius-sm)'
                        }} />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>

          <Separator />

          {/* Components Section Header */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <h2 style={{ 
              fontSize: 'var(--font-size-xl)',
              fontWeight: 'var(--font-weight-semibold)',
              color: 'var(--primary)'
            }}>
              UI Components
            </h2>
            <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
              Reusable components from FT Design System with CSS variable styling
            </p>
          </div>
        </div>
      </div>

      {/* Component List - Full Width */}
      <div style={{ 
        maxWidth: '1440px',
        margin: '0 auto',
        padding: '0 var(--space-8) var(--space-8)'
      }}>
        {components.map((component) => (
          <ComponentCard key={component.name} {...component} />
        ))}
      </div>
    </div>
  );
}
