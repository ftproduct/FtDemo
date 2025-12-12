'use client';

/**
 * FT Design System v4.15.20 Complete Showcase
 * 124 Components • Pure Tailwind Classes • No Forbidden Patterns
 */

import React, { useState } from 'react';
import { AppHeader } from '@/components/layout/app-header';
import { AppSidebar } from '@/components/layout/app-sidebar';
import {
  // Core
  designTokens,
  
  // Layout & Structure
  Card, CardHeader, CardTitle, CardDescription, CardBody, CardFooter,
  Divider, Spacer,
  Grid, Row, Col,
  
  // Typography
  Text, Typography, SubText,
  
  // Buttons
  Button, ButtonGroup, ButtonGroupItem,
  Toggle, ToggleGroup,
  FloatButton, FloatButtonGroup,
  
  // Form Components
  Input, InputLabel, InputField,
  InputNumber, InputNumberField,
  Textarea, TextareaLabel, TextareaField,
  Select, SelectTrigger, SelectContent, SelectItem, SelectValue,
  Checkbox, CheckboxInput, CheckboxLabel,
  RadioGroup, RadioItemInput, RadioItemLabel,
  RadioSelector, RadioSelectorOption,
  Switch, SwitchInput, SwitchLabel,
  Slider,
  DatePicker, TimePicker, ColorPicker,
  Rate, RateItem, RateIcon,
  Cascader,
  TreeSelect,
  Transfer,
  Mentions,
  Form,
  
  // Display Components
  Badge, Chicklet,
  Alert, AlertTitle, AlertDescription,
  Avatar, AvatarFallback, AvatarGroup,
  Progress, ProgressBar,
  Spin, Loader,
  Skeleton, SkeletonText, SkeletonImage,
  Empty, Result,
  Statistic, StatisticTitle, StatisticValue,
  Image,
  
  // Navigation
  Tabs, TabsList, TabsTrigger, TabsContent,
  SegmentedTabs, SegmentedTabItem,
  Breadcrumb, BreadcrumbList, BreadcrumbItem, BreadcrumbLink,
  Pagination,
  Steps, StepsList, StepItem, StepTitle, StepDescription,
  Anchor, BackTop,
  
  // Data Display
  Table, TableHeader, TableBody, TableRow, TableHead, TableCell,
  List, ListItem, ListItemContent,
  Timeline, TimelineItem, TimelineDot, TimelineContent,
  Descriptions, DescriptionsItem,
  Tree,
  
  // Overlays & Feedback
  Modal, ModalTrigger, ModalContent, ModalHeader, ModalTitle, ModalDescription, ModalBody, ModalFooter,
  Drawer, DrawerTrigger, DrawerContent, DrawerHeader, DrawerTitle, DrawerDescription, DrawerBody, DrawerFooter,
  Tooltip, TooltipTrigger, TooltipContent, TooltipProvider,
  Popconfirm, PopconfirmTrigger, PopconfirmContent,
  HoverCard, HoverCardTrigger, HoverCardContent,
  message,
  
  // Complex Components
  Collapsible, CollapsibleTrigger, CollapsibleHeader, CollapsibleTitle, CollapsibleIcon, CollapsibleContent, CollapsibleExtra,
  Collapse,
  Dropdown, DropdownTrigger, DropdownContent, DropdownMenu, DropdownMenuItem, DropdownMenuList,
  Calendar,
  Carousel,
  Upload, UploadButton,
  Watermark,
  Affix,
  ThemeSwitch,
  Tour,
  Icon,
} from 'ft-design-system';

export default function ComponentsShowcasePage() {
  const [activeTab, setActiveTab] = useState('forms');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  return (
    <div className="flex flex-col min-h-screen">
      <AppHeader />
      <div className="flex flex-1">
        <AppSidebar />
        <main className="flex-1 p-6 ml-[250px] bg-neutral-50">
          {/* Page Header */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-2">
              <h1 className="text-xxl-rem font-bold text-primary-700">
                FT Design System Complete Showcase
              </h1>
              <Badge variant="success">v4.15.20</Badge>
            </div>
            <p className="text-md-rem text-neutral-600">
              102+ components • Pure Tailwind classes • No forbidden patterns
            </p>
          </div>

          {/* Main Navigation Tabs */}
          <Tabs defaultValue={activeTab}>
            <TabsList className="mb-6">
              <TabsTrigger value="forms" onClick={() => setActiveTab('forms')}>Forms</TabsTrigger>
              <TabsTrigger value="dataDisplay" onClick={() => setActiveTab('dataDisplay')}>Data Display</TabsTrigger>
              <TabsTrigger value="navigation" onClick={() => setActiveTab('navigation')}>Navigation</TabsTrigger>
              <TabsTrigger value="feedback" onClick={() => setActiveTab('feedback')}>Feedback</TabsTrigger>
              <TabsTrigger value="general" onClick={() => setActiveTab('general')}>General</TabsTrigger>
              <TabsTrigger value="tokens" onClick={() => setActiveTab('tokens')}>Tokens</TabsTrigger>
            </TabsList>

            <TabsContent value="forms">
              <FormsShowcase />
            </TabsContent>

            <TabsContent value="dataDisplay">
              <DataDisplayShowcase />
            </TabsContent>

            <TabsContent value="navigation">
              <NavigationShowcase />
            </TabsContent>

            <TabsContent value="feedback">
              <FeedbackShowcase 
                isModalOpen={isModalOpen} 
                setIsModalOpen={setIsModalOpen} 
                isDrawerOpen={isDrawerOpen} 
                setIsDrawerOpen={setIsDrawerOpen} 
              />
            </TabsContent>

            <TabsContent value="general">
              <GeneralShowcase />
            </TabsContent>

            <TabsContent value="tokens">
              <TokensShowcase />
            </TabsContent>
          </Tabs>
        </main>
      </div>
    </div>
  );
}

// FORMS SHOWCASE
function FormsShowcase() {
  return (
    <div className="flex flex-col gap-6">
      <ShowcaseSection title="Text Inputs" description="Input fields and text areas">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Input>
            <InputLabel>Text Input</InputLabel>
            <InputField placeholder="Enter text..." size="md" />
          </Input>

          <InputNumber>
            <InputLabel>Number Input</InputLabel>
            <InputNumberField placeholder="Enter number..." size="md" />
          </InputNumber>

          <Textarea>
            <TextareaLabel>Textarea</TextareaLabel>
            <TextareaField placeholder="Enter multiline text..." rows={3} size="md" />
          </Textarea>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Selection Controls" description="Select, checkbox, radio, and switch">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">
              Select Dropdown
            </label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Select option" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">Option 1</SelectItem>
                <SelectItem value="2">Option 2</SelectItem>
                <SelectItem value="3">Option 3</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div>
            <p className="mb-2 font-semibold text-sm-rem text-primary-700">Checkboxes</p>
            <div className="flex flex-col gap-2">
              <div className="flex items-center gap-2">
                <CheckboxInput id="cb1" />
                <CheckboxLabel htmlFor="cb1">Checkbox 1</CheckboxLabel>
              </div>
              <div className="flex items-center gap-2">
                <CheckboxInput id="cb2" />
                <CheckboxLabel htmlFor="cb2">Checkbox 2</CheckboxLabel>
              </div>
            </div>
          </div>

          <div>
            <p className="mb-2 font-semibold text-sm-rem text-primary-700">Radio Group</p>
            <RadioGroup name="radio-demo" defaultValue="r1">
              <div className="flex items-center gap-2">
                <RadioItemInput value="r1" id="r1" />
                <RadioItemLabel htmlFor="r1">Radio 1</RadioItemLabel>
              </div>
              <div className="flex items-center gap-2">
                <RadioItemInput value="r2" id="r2" />
                <RadioItemLabel htmlFor="r2">Radio 2</RadioItemLabel>
              </div>
            </RadioGroup>
          </div>
        </div>

        <div className="mt-4">
          <p className="mb-2 font-semibold text-sm-rem text-primary-700">Radio Selector</p>
          <RadioSelector defaultValue="option1">
            <RadioSelectorOption value="option1">Option 1</RadioSelectorOption>
            <RadioSelectorOption value="option2">Option 2</RadioSelectorOption>
            <RadioSelectorOption value="option3">Option 3</RadioSelectorOption>
          </RadioSelector>
        </div>

        <div className="mt-4 flex items-center gap-2">
          <SwitchInput id="sw1" />
          <SwitchLabel htmlFor="sw1">Toggle Switch</SwitchLabel>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Advanced Inputs" description="Date, time, color, rating, and slider">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">
              Date Picker
            </label>
            <DatePicker placeholder="Select date" size="md" />
          </div>

          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">
              Time Picker
            </label>
            <TimePicker placeholder="Select time" size="md" />
          </div>

          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">
              Color Picker
            </label>
            <ColorPicker />
          </div>

          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">
              Rate
            </label>
            <Rate defaultValue={3} />
          </div>
        </div>

        <div className="mt-4">
          <label className="block mb-2 text-sm-rem font-medium text-primary-700">
            Slider
          </label>
          <Slider defaultValue={50} min={0} max={100} />
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Upload" description="File upload components">
        <UploadButton>Upload File</UploadButton>
      </ShowcaseSection>

      <ShowcaseSection title="Advanced Form Components" description="Cascader, TreeSelect, Transfer, Mentions">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">Cascader</label>
            <Cascader placeholder="Select cascading option" size="md" />
          </div>
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">TreeSelect</label>
            <TreeSelect placeholder="Select from tree" size="md" />
          </div>
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">Transfer</label>
            <Transfer />
          </div>
          <div>
            <label className="block mb-2 text-sm-rem font-medium text-primary-700">Mentions</label>
            <Mentions placeholder="Type @ to mention" size="md" />
          </div>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Rate Component (Composable API)" description="Star rating with composable API">
        <Rate defaultValue={3}>
          <RateItem value={1} />
          <RateItem value={2} />
          <RateItem value={3} />
          <RateItem value={4} />
          <RateItem value={5} />
        </Rate>
      </ShowcaseSection>

      <ShowcaseSection title="Form Component" description="Form wrapper with validation">
        <Form onSubmit={(e) => { e.preventDefault(); console.log('Form submitted'); }}>
          <div className="flex flex-col gap-4">
            <Input>
              <InputLabel>Form Input</InputLabel>
              <InputField placeholder="Enter value" size="md" />
            </Input>
            <Button variant="primary" size="md" type="submit">Submit Form</Button>
          </div>
        </Form>
      </ShowcaseSection>
    </div>
  );
}

// DATA DISPLAY SHOWCASE
function DataDisplayShowcase() {
  // Table data with required 'id' field
  const tableData = [
    { id: 1, name: 'Item 1', status: 'Active', value: '$1,234' },
    { id: 2, name: 'Item 2', status: 'Pending', value: '$5,678' },
    { id: 3, name: 'Item 3', status: 'Inactive', value: '$910' },
  ];

  // Table columns with 'title' not 'header'
  const tableColumns = [
    { key: 'name', title: 'Name' },
    { 
      key: 'status', 
      title: 'Status',
      render: (value: string) => (
        <Badge variant={value === 'Active' ? 'success' : value === 'Pending' ? 'warning' : 'danger'}>
          {value}
        </Badge>
      )
    },
    { key: 'value', title: 'Value' },
  ];

  return (
    <div className="flex flex-col gap-6">
      <ShowcaseSection title="Tables" description="Data tables with proper API">
        <Table columns={tableColumns} data={tableData} />
      </ShowcaseSection>

      <ShowcaseSection title="Table (Composable API)" description="Table with sub-components">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Value</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell>Item 1</TableCell>
              <TableCell><Badge variant="success">Active</Badge></TableCell>
              <TableCell>$1,234</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>Item 2</TableCell>
              <TableCell><Badge variant="warning">Pending</Badge></TableCell>
              <TableCell>$5,678</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </ShowcaseSection>

      <ShowcaseSection title="Lists" description="List components">
        <List>
          <ListItem>
            <ListItemContent>
              <div>
                <div className="font-semibold text-primary-700">List Item 1</div>
                <div className="text-sm-rem text-neutral-500">Description text</div>
              </div>
            </ListItemContent>
          </ListItem>
          <ListItem>
            <ListItemContent>
              <div>
                <div className="font-semibold text-primary-700">List Item 2</div>
                <div className="text-sm-rem text-neutral-500">Description text</div>
              </div>
            </ListItemContent>
          </ListItem>
          <ListItem>
            <ListItemContent>
              <div>
                <div className="font-semibold text-primary-700">List Item 3</div>
                <div className="text-sm-rem text-neutral-500">Description text</div>
              </div>
            </ListItemContent>
          </ListItem>
        </List>
      </ShowcaseSection>

      <ShowcaseSection title="Timeline" description="Event timelines">
        <Timeline>
          <TimelineItem>
            <TimelineDot color="primary" />
            <TimelineContent>
              <h4 className="font-semibold text-primary-700">Event 1</h4>
              <p className="text-sm-rem text-neutral-500">Event description</p>
            </TimelineContent>
          </TimelineItem>
          <TimelineItem>
            <TimelineDot color="success" />
            <TimelineContent>
              <h4 className="font-semibold text-primary-700">Event 2</h4>
              <p className="text-sm-rem text-neutral-500">Event description</p>
            </TimelineContent>
          </TimelineItem>
          <TimelineItem>
            <TimelineDot color="warning" />
            <TimelineContent>
              <h4 className="font-semibold text-primary-700">Event 3</h4>
              <p className="text-sm-rem text-neutral-500">Event description</p>
            </TimelineContent>
          </TimelineItem>
        </Timeline>
      </ShowcaseSection>

      <ShowcaseSection title="Descriptions" description="Key-value pairs">
        <Descriptions>
          <DescriptionsItem label="Name">John Doe</DescriptionsItem>
          <DescriptionsItem label="Email">john@example.com</DescriptionsItem>
          <DescriptionsItem label="Status"><Badge variant="success">Active</Badge></DescriptionsItem>
          <DescriptionsItem label="Role">Administrator</DescriptionsItem>
        </Descriptions>
      </ShowcaseSection>

      <ShowcaseSection title="Collapsible" description="Expandable content panels">
        <div className="flex flex-col gap-4">
          <Collapsible type="Primary" bg="Secondary">
            <CollapsibleTrigger>
              <CollapsibleHeader>
                <CollapsibleIcon />
                <CollapsibleTitle>Primary Collapsible</CollapsibleTitle>
              </CollapsibleHeader>
            </CollapsibleTrigger>
            <CollapsibleContent>
              <p className="p-4 text-primary-700">
                Content inside collapsible panel using FT DS components.
              </p>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible type="Secondary" bg="Primary">
            <CollapsibleTrigger>
              <CollapsibleHeader>
                <CollapsibleIcon />
                <CollapsibleTitle>Secondary Collapsible with Badge</CollapsibleTitle>
                <CollapsibleExtra>
                  <Badge variant="primary">New</Badge>
                </CollapsibleExtra>
              </CollapsibleHeader>
            </CollapsibleTrigger>
            <CollapsibleContent>
              <p className="p-4 text-primary-700">
                Another collapsible panel with extra badge component.
              </p>
            </CollapsibleContent>
          </Collapsible>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Tree" description="Tree structure component">
        <Tree
          treeData={[
            { key: '1', title: 'Node 1', children: [{ key: '1-1', title: 'Node 1-1' }] },
            { key: '2', title: 'Node 2', children: [{ key: '2-1', title: 'Node 2-1' }] },
          ]}
        />
      </ShowcaseSection>

      <ShowcaseSection title="Image" description="Image component with loading states">
        <div className="flex gap-4">
          <Image 
            src="https://via.placeholder.com/200x200" 
            alt="Placeholder"
            width={200}
            height={200}
            className="rounded-lg"
          />
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Carousel" description="Image/content carousel">
        <Carousel>
          <div className="h-48 bg-primary-100 rounded-lg flex items-center justify-center text-primary-700">Slide 1</div>
          <div className="h-48 bg-secondary-100 rounded-lg flex items-center justify-center text-primary-700">Slide 2</div>
          <div className="h-48 bg-neutral-100 rounded-lg flex items-center justify-center text-primary-700">Slide 3</div>
        </Carousel>
      </ShowcaseSection>
    </div>
  );
}

// NAVIGATION SHOWCASE
function NavigationShowcase() {
  return (
    <div className="flex flex-col gap-6">
      <ShowcaseSection title="Tabs" description="Tab navigation">
        <div>
          <p className="mb-2 font-semibold text-sm-rem text-primary-700">Segmented Tabs</p>
          <SegmentedTabs defaultValue="tab1">
            <SegmentedTabItem value="tab1">Tab 1</SegmentedTabItem>
            <SegmentedTabItem value="tab2">Tab 2</SegmentedTabItem>
            <SegmentedTabItem value="tab3">Tab 3</SegmentedTabItem>
          </SegmentedTabs>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Breadcrumb" description="Navigation breadcrumbs">
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink href="/">Home</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbItem>
              <BreadcrumbLink href="/components">Components</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbItem>
              <span>Current Page</span>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
      </ShowcaseSection>

      <ShowcaseSection title="Pagination" description="Page navigation">
        <Pagination total={100} pageSize={10} current={1} />
      </ShowcaseSection>

      <ShowcaseSection title="Steps" description="Progress steps">
        <Steps current={1}>
          <StepsList>
            <StepItem>
              <StepTitle>Step 1</StepTitle>
              <StepDescription>First step description</StepDescription>
            </StepItem>
            <StepItem>
              <StepTitle>Step 2</StepTitle>
              <StepDescription>Second step description</StepDescription>
            </StepItem>
            <StepItem>
              <StepTitle>Step 3</StepTitle>
              <StepDescription>Third step description</StepDescription>
            </StepItem>
          </StepsList>
        </Steps>
      </ShowcaseSection>

      <ShowcaseSection title="Anchor" description="Anchor links">
        <Anchor
          items={[
            { key: 'section1', href: '#section1', title: 'Section 1' },
            { key: 'section2', href: '#section2', title: 'Section 2' },
            { key: 'section3', href: '#section3', title: 'Section 3' },
          ]}
        />
      </ShowcaseSection>

      <ShowcaseSection title="BackTop" description="Back to top button">
        <BackTop />
      </ShowcaseSection>
    </div>
  );
}

// FEEDBACK SHOWCASE
function FeedbackShowcase({ 
  isModalOpen, 
  setIsModalOpen, 
  isDrawerOpen, 
  setIsDrawerOpen 
}: {
  isModalOpen: boolean;
  setIsModalOpen: (open: boolean) => void;
  isDrawerOpen: boolean;
  setIsDrawerOpen: (open: boolean) => void;
}) {
  return (
    <div className="flex flex-col gap-6">
      <ShowcaseSection title="Alerts" description="Alert messages">
        <div className="flex flex-col gap-3">
          <Alert variant="info">
            <AlertTitle>Information</AlertTitle>
            <AlertDescription>This is an informational alert message.</AlertDescription>
          </Alert>
          <Alert variant="success">
            <AlertTitle>Success</AlertTitle>
            <AlertDescription>Operation completed successfully.</AlertDescription>
          </Alert>
          <Alert variant="warning">
            <AlertTitle>Warning</AlertTitle>
            <AlertDescription>Please review before proceeding.</AlertDescription>
          </Alert>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Progress & Loading" description="Progress indicators">
        <div className="flex flex-col gap-3">
          <Progress value={50} max={100} />
          <Progress value={75} max={100} status="success" />
          <Progress value={90} max={100} status="warning" />
        </div>

        <div className="flex gap-4 mt-4 items-center">
          <Spin size="sm" />
          <Spin size="md" />
          <Spin size="lg" />
          <Loader />
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Skeleton Loaders" description="Loading placeholders">
        <div className="flex flex-col gap-3">
          <Skeleton className="w-full h-5" />
          <Skeleton className="w-4/5 h-5" />
          <SkeletonText lines={3} />
          <div className="flex gap-3">
            <SkeletonImage className="w-24 h-24" />
            <SkeletonImage className="w-24 h-24" />
          </div>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Modal & Drawer" description="Overlay components">
        <div className="flex gap-3">
          <Modal open={isModalOpen} onOpenChange={setIsModalOpen}>
            <ModalTrigger asChild>
              <Button variant="primary" size="md">Open Modal</Button>
            </ModalTrigger>
            <ModalContent>
              <ModalHeader>
                <ModalTitle>Modal Title</ModalTitle>
                <ModalDescription>Modal description text</ModalDescription>
              </ModalHeader>
              <ModalBody>
                <p className="text-primary-700">This is the modal content.</p>
              </ModalBody>
              <ModalFooter>
                <Button variant="secondary" size="md" onClick={() => setIsModalOpen(false)}>Cancel</Button>
                <Button variant="primary" size="md" onClick={() => setIsModalOpen(false)}>Confirm</Button>
              </ModalFooter>
            </ModalContent>
          </Modal>

          <Drawer open={isDrawerOpen} onOpenChange={setIsDrawerOpen}>
            <DrawerTrigger asChild>
              <Button variant="secondary" size="md">Open Drawer</Button>
            </DrawerTrigger>
            <DrawerContent>
              <DrawerHeader>
                <DrawerTitle>Drawer Title</DrawerTitle>
                <DrawerDescription>Drawer description</DrawerDescription>
              </DrawerHeader>
              <DrawerBody>
                <p className="text-primary-700">Drawer content goes here.</p>
              </DrawerBody>
              <DrawerFooter>
                <Button variant="secondary" size="md" onClick={() => setIsDrawerOpen(false)}>Close</Button>
              </DrawerFooter>
            </DrawerContent>
          </Drawer>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Tooltips & Popovers" description="Contextual information">
        <TooltipProvider>
          <div className="flex gap-3">
            <Tooltip>
              <TooltipTrigger asChild>
                <Button variant="secondary" size="md">Hover for Tooltip</Button>
              </TooltipTrigger>
              <TooltipContent>
                <p>Helpful tooltip message</p>
              </TooltipContent>
            </Tooltip>

            <Popconfirm>
              <PopconfirmTrigger asChild>
                <Button variant="destructive" size="md">Delete Item</Button>
              </PopconfirmTrigger>
              <PopconfirmContent>
                <p className="text-primary-700">Are you sure you want to delete?</p>
              </PopconfirmContent>
            </Popconfirm>
          </div>
        </TooltipProvider>
      </ShowcaseSection>

      <ShowcaseSection title="Empty States" description="No data indicators">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Empty description="No data available" />
          <Result status="success" title="Success!" description="Operation completed successfully" />
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="HoverCard" description="Card shown on hover">
        <HoverCard>
          <HoverCardTrigger asChild>
            <Button variant="secondary" size="md">Hover me</Button>
          </HoverCardTrigger>
          <HoverCardContent>
            <div className="p-4">
              <h4 className="font-semibold text-primary-700 mb-2">Hover Card Title</h4>
              <p className="text-sm-rem text-neutral-500">This content appears on hover.</p>
            </div>
          </HoverCardContent>
        </HoverCard>
      </ShowcaseSection>

      <ShowcaseSection title="Message/Notification" description="Toast notifications">
        <div className="flex gap-3">
          <Button 
            variant="primary" 
            size="md"
            onClick={() => message.success('Success message!')}
          >
            Show Success
          </Button>
          <Button 
            variant="secondary" 
            size="md"
            onClick={() => message.error('Error message!')}
          >
            Show Error
          </Button>
          <Button 
            variant="secondary" 
            size="md"
            onClick={() => message.info('Info message!')}
          >
            Show Info
          </Button>
        </div>
      </ShowcaseSection>
    </div>
  );
}

// GENERAL SHOWCASE
function GeneralShowcase() {
  return (
    <div className="flex flex-col gap-6">
      <ShowcaseSection title="Buttons" description="Button variants and sizes">
        <div className="flex gap-3 flex-wrap">
          <Button variant="primary" size="md">Primary</Button>
          <Button variant="secondary" size="md">Secondary</Button>
          <Button variant="ghost" size="md">Ghost</Button>
          <Button variant="text" size="md">Text</Button>
          <Button variant="link" size="md">Link</Button>
          <Button variant="destructive" size="md">Destructive</Button>
          <Button variant="primary" size="md" disabled>Disabled</Button>
        </div>

        <div className="mt-4">
          <p className="mb-2 text-sm-rem font-semibold text-primary-700">Button Sizes</p>
          <div className="flex gap-3 items-center">
            <Button variant="primary" size="sm">Small</Button>
            <Button variant="primary" size="md">Medium</Button>
            <Button variant="primary" size="lg">Large</Button>
          </div>
        </div>

        <div className="mt-4">
          <p className="mb-2 text-sm-rem font-semibold text-primary-700">Button Group</p>
          <ButtonGroup>
            <ButtonGroupItem>Option 1</ButtonGroupItem>
            <ButtonGroupItem>Option 2</ButtonGroupItem>
            <ButtonGroupItem>Option 3</ButtonGroupItem>
          </ButtonGroup>
        </div>

        <div className="mt-4">
          <p className="mb-2 text-sm-rem font-semibold text-primary-700">Toggle Group</p>
          <ToggleGroup type="single">
            <Toggle value="left">Left</Toggle>
            <Toggle value="center">Center</Toggle>
            <Toggle value="right">Right</Toggle>
          </ToggleGroup>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Badges & Chicklets" description="Status indicators">
        <div className="flex gap-3 flex-wrap mb-4">
          <Badge variant="primary">Primary</Badge>
          <Badge variant="secondary">Secondary</Badge>
          <Badge variant="success">Success</Badge>
          <Badge variant="warning">Warning</Badge>
          <Badge variant="danger">Danger</Badge>
          <Badge variant="neutral">Neutral</Badge>
        </div>

        <div>
          <p className="mb-2 text-sm-rem font-semibold text-primary-700">Chicklets</p>
          <div className="flex gap-2 flex-wrap">
            <Chicklet variant="primary" state="default">Active</Chicklet>
            <Chicklet variant="success" state="default">Completed</Chicklet>
            <Chicklet variant="warning" state="default">Pending</Chicklet>
          </div>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Avatars" description="User profile images">
        <div className="flex gap-4 items-center">
          <Avatar size="sm"><AvatarFallback>SM</AvatarFallback></Avatar>
          <Avatar size="md"><AvatarFallback>MD</AvatarFallback></Avatar>
          <Avatar size="lg"><AvatarFallback>LG</AvatarFallback></Avatar>
        </div>

        <div className="mt-4">
          <p className="mb-2 text-sm-rem font-semibold text-primary-700">Avatar Group</p>
          <AvatarGroup max={3}>
            <Avatar size="md"><AvatarFallback>AB</AvatarFallback></Avatar>
            <Avatar size="md"><AvatarFallback>CD</AvatarFallback></Avatar>
            <Avatar size="md"><AvatarFallback>EF</AvatarFallback></Avatar>
            <Avatar size="md"><AvatarFallback>GH</AvatarFallback></Avatar>
          </AvatarGroup>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Typography" description="Text components">
        <div className="flex flex-col gap-2">
          <Text>Default text component</Text>
          <Typography variant="h1">Typography Heading 1</Typography>
          <Typography variant="h2">Typography Heading 2</Typography>
          <SubText>Secondary text</SubText>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Statistics" description="Numerical data display">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardBody>
              <Statistic>
                <StatisticTitle>Revenue</StatisticTitle>
                <StatisticValue>$1,234,567</StatisticValue>
              </Statistic>
            </CardBody>
          </Card>
          <Card>
            <CardBody>
              <Statistic>
                <StatisticTitle>Users</StatisticTitle>
                <StatisticValue>12,345</StatisticValue>
              </Statistic>
            </CardBody>
          </Card>
          <Card>
            <CardBody>
              <Statistic>
                <StatisticTitle>Orders</StatisticTitle>
                <StatisticValue>6,789</StatisticValue>
              </Statistic>
            </CardBody>
          </Card>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="FloatButton & FloatButtonGroup" description="Floating action buttons">
        <div className="flex gap-4 items-center">
          <FloatButton icon="add" />
          <FloatButtonGroup>
            <FloatButton icon="add" />
            <FloatButton icon="edit" />
            <FloatButton icon="delete" />
          </FloatButtonGroup>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Collapse" description="Collapsible panel alternative">
        <Collapse items={[
          { key: '1', label: 'Panel 1', children: <p className="p-4 text-primary-700">Content for panel 1</p> },
          { key: '2', label: 'Panel 2', children: <p className="p-4 text-primary-700">Content for panel 2</p> },
        ]} />
      </ShowcaseSection>

      <ShowcaseSection title="Dropdown Menu" description="Dropdown with menu items">
        <Dropdown>
          <DropdownTrigger asChild>
            <Button variant="secondary" size="md">Open Menu</Button>
          </DropdownTrigger>
          <DropdownContent>
            <DropdownMenu>
              <DropdownMenuList>
                <DropdownMenuItem>Menu Item 1</DropdownMenuItem>
                <DropdownMenuItem>Menu Item 2</DropdownMenuItem>
                <DropdownMenuItem>Menu Item 3</DropdownMenuItem>
              </DropdownMenuList>
            </DropdownMenu>
          </DropdownContent>
        </Dropdown>
      </ShowcaseSection>

      <ShowcaseSection title="Grid System" description="Layout grid with Row and Col">
        <Grid>
          <Row>
            <Col span={6}>
              <Card>
                <CardBody>
                  <p className="text-primary-700">Column 1 (50%)</p>
                </CardBody>
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <CardBody>
                  <p className="text-primary-700">Column 2 (50%)</p>
                </CardBody>
              </Card>
            </Col>
          </Row>
        </Grid>
      </ShowcaseSection>

      <ShowcaseSection title="Progress Bar" description="Progress bar component">
        <ProgressBar value={60} max={100} />
      </ShowcaseSection>

      <ShowcaseSection title="Watermark" description="Watermark overlay">
        <div className="relative h-48 bg-neutral-50 rounded-lg p-4">
          <Watermark content="FT Design System" />
          <p className="text-primary-700">Content with watermark</p>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Affix" description="Fixed positioning component">
        <Affix offsetTop={100}>
          <Button variant="primary" size="md">Fixed Button</Button>
        </Affix>
      </ShowcaseSection>

      <ShowcaseSection title="ThemeSwitch" description="Theme switcher">
        <ThemeSwitch />
      </ShowcaseSection>

      <ShowcaseSection title="Tour" description="User tour/onboarding">
        <Tour
          steps={[
            { target: '#tour-target-1', title: 'Step 1', content: 'This is the first step' },
            { target: '#tour-target-2', title: 'Step 2', content: 'This is the second step' },
          ]}
        />
        <div id="tour-target-1" className="p-4 bg-neutral-100 rounded-lg mb-4">Tour Target 1</div>
        <div id="tour-target-2" className="p-4 bg-neutral-100 rounded-lg">Tour Target 2</div>
      </ShowcaseSection>

      <ShowcaseSection title="Icon Component" description="Icon display">
        <div className="flex gap-4 items-center flex-wrap">
          <Icon name="check" size={24} />
          <Icon name="close" size={24} />
          <Icon name="arrow-right" size={24} />
          <Icon name="arrow-left" size={24} />
          <Icon name="search" size={24} />
          <Icon name="filter" size={24} />
          <Icon name="settings" size={24} />
          <Icon name="bell" size={24} />
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Other" description="Miscellaneous components">
        <div className="flex flex-col gap-4">
          <Divider />
          <Spacer />
          <Calendar />
        </div>
      </ShowcaseSection>
    </div>
  );
}

// TOKENS SHOWCASE
function TokensShowcase() {
  const colors = designTokens.baseColors.lightMode;
  const colorGroups = [
    { name: 'Primary', prefix: 'primary' },
    { name: 'Secondary', prefix: 'secondary' },
    { name: 'Tertiary', prefix: 'tertiary' },
    { name: 'Neutral', prefix: 'neutral' },
    { name: 'Positive', prefix: 'positive' },
    { name: 'Warning', prefix: 'warning' },
    { name: 'Danger', prefix: 'danger' },
  ];

  return (
    <div className="flex flex-col gap-6">
      {colorGroups.map((group) => (
        <ShowcaseSection key={group.prefix} title={`${group.name} Colors`} description={`${group.name} color palette`}>
          <div className="grid grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-3">
            {[100, 200, 300, 400, 500, 600, 700, 800, 900].map((shade) => {
              const colorKey = `${group.prefix}${shade}` as keyof typeof colors;
              const colorValue = colors[colorKey];
              
              if (!colorValue) return null;

              return (
                <div key={colorKey} className="flex flex-col items-center gap-2">
                  <div
                    className="w-full aspect-square rounded-lg border border-neutral-200"
                    style={{ backgroundColor: colorValue }}
                  />
                  <div className="text-center">
                    <div className="text-sm-rem font-semibold text-primary-700">{shade}</div>
                    <div className="text-xs-rem text-neutral-500 font-mono">{colorValue}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </ShowcaseSection>
      ))}

      <ShowcaseSection title="Typography Scale" description="Font sizes using rem-based classes">
        <div className="flex flex-col gap-4">
          <div>
            <span className="text-xs-rem text-primary-700">text-xs-rem (12px)</span>
          </div>
          <div>
            <span className="text-sm-rem text-primary-700">text-sm-rem (14px)</span>
          </div>
          <div>
            <span className="text-md-rem text-primary-700">text-md-rem (16px)</span>
          </div>
          <div>
            <span className="text-lg-rem text-primary-700">text-lg-rem (20px)</span>
          </div>
          <div>
            <span className="text-xl-rem text-primary-700">text-xl-rem (24px)</span>
          </div>
          <div>
            <span className="text-xxl-rem text-primary-700">text-xxl-rem (28px)</span>
          </div>
        </div>
      </ShowcaseSection>

      <ShowcaseSection title="Color Classes" description="Semantic color utilities">
        <div className="flex flex-col gap-3">
          <div className="p-3 bg-primary-700 text-white rounded-lg">bg-primary-700</div>
          <div className="p-3 bg-neutral-100 text-primary-700 rounded-lg border">bg-neutral-100</div>
          <div className="p-3 border border-neutral-300 rounded-lg">
            <span className="text-positive">text-positive</span>
            <span className="mx-2">|</span>
            <span className="text-critical">text-critical</span>
            <span className="mx-2">|</span>
            <span className="text-warning-600">text-warning-600</span>
          </div>
        </div>
      </ShowcaseSection>
    </div>
  );
}

// HELPER COMPONENT
function ShowcaseSection({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardBody>{children}</CardBody>
    </Card>
  );
}
