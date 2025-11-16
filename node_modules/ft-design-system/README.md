# FT Design System

A comprehensive React component library built from Figma designs, featuring a modern design system with TypeScript and Tailwind CSS support.

## 🚀 Features

- 🎨 **Figma-First Design**: Components built directly from Figma specifications
- 📦 **12+ Core Components**: Including Table, Button, Badge, Checkbox, and more
- 🎯 **190+ Icons**: Comprehensive icon library with proper TypeScript definitions
- 💅 **Tailwind CSS**: Built with Tailwind for easy customization
- 📱 **Responsive**: Mobile-friendly and responsive design
- 🔍 **TypeScript**: Full TypeScript support with proper type definitions
- 📚 **Storybook**: Complete documentation and component playground

## 📥 Installation

```bash
# Install from npm
npm install ft-design-system
```

## 🔧 Usage

```tsx
// Import components
import { Table, Button, Badge, Checkbox } from 'ft-design-system';

// Import styles in your main App.tsx or index.tsx
import 'ft-design-system/dist/styles.css';

function App() {
  return (
    <div>
      <Button variant="primary">Click me</Button>
      <Badge variant="default">Status</Badge>
    </div>
  );
}
```

## 🚨 Troubleshooting

### CSS Styles Not Loading

If you're getting errors like `"Cannot resolve 'ft-design-system/dist/styles.css'"`, try these solutions:

1. **Clear Cache and Reinstall**:
   ```bash
   rm -rf node_modules package-lock.json
   npm install ft-design-system@latest
   ```

2. **Verify Package Version**:
   ```bash
   npm list ft-design-system
   # Should show version 4.2.3 or higher
   ```

3. **Alternative Import Methods**:
   ```tsx
   // Method 1: Direct path
   import 'ft-design-system/dist/styles.css';
   
   // Method 2: If above fails, try importing from specific path
   import 'ft-design-system/styles.css';
   
   // Method 3: For Next.js projects, import in _app.tsx or layout.tsx
   import 'ft-design-system/dist/styles.css';
   ```

4. **For Vite Projects**, ensure your `vite.config.js` includes:
   ```js
   export default defineConfig({
     optimizeDeps: {
       include: ['ft-design-system']
     }
   });
   ```

5. **For Webpack Projects**, ensure CSS loader is configured:
   ```js
   module.exports = {
     module: {
       rules: [
         {
           test: /\.css$/,
           use: ['style-loader', 'css-loader']
         }
       ]
     }
   };
   ```

### Icon Import Issues

If icons are not working:
```tsx
// Import individual icons (recommended)
import { ChevronLeft, Play, User } from 'ft-design-system';

// Or use the generic Icon component
import { Icon } from 'ft-design-system';
<Icon name="chevron-left" />
```

## 🧩 Components

### Table
- Primary and Secondary variants
- Sorting functionality
- Row selection with indeterminate state
- Pagination support
- Custom cell rendering

```tsx
<Table 
  variant="primary"
  data={data}
  columns={columns}
  onSort={handleSort}
  selectable
/>
```

### Button
- Multiple variants: primary, secondary, destructive, link
- Loading state
- Icon support
- Size variants

```tsx
<Button variant="primary" loading={false}>
  Click me
</Button>
```

### Badge
- Status indicators
- Multiple variants
- Icon support

### Checkbox
- Indeterminate state
- Custom styling
- Label support

### Input
- Text input with validation
- Icon support
- Error states

### DatePicker
- Date selection
- Range selection
- Custom formatting

### Icons
- 190+ custom icons
- SVG-based
- Color inheritance
- Size customization

## 🎨 Design Guidelines

For designers working with the FT Design System, please follow our comprehensive design guidelines to ensure consistency and proper integration:

📋 **[Design Guidelines](DESIGN_GUIDELINES.md)** - Essential rules for creating components that match the FT Design System

Key highlights:
- **Unified Component Sizing**: All components follow standardized heights (36px, 44px, 52px, 64px)
- **Color System**: Use #434f64 as primary brand color
- **Consistent Spacing**: Standardized padding and margin ratios
- **Accessibility**: WCAG AA compliance requirements
- **Developer Handoff**: Proper specifications and naming conventions

## 🎨 Customization

### Tailwind Configuration
The design system uses Tailwind CSS for styling. Make sure your project has Tailwind CSS configured:

```js
// tailwind.config.js
module.exports = {
  content: [
    './src/**/*.{js,jsx,ts,tsx}',
    './node_modules/ft-design-system/**/*.{js,jsx,ts,tsx}'
  ],
  // ... rest of your config
}
```

## 🛠️ Development

```bash
# Install dependencies
npm install

# Run Storybook
npm run storybook

# Build package
npm run build

# Run tests
npm run test

# Share with designers (local network)
npm run share-storybook

# Setup automatic deployment to Netlify
npm run setup-deployment
```

### 📋 Maintaining Documentation

When adding or modifying components, always update the downloadable documentation:

```bash
# Check if docs are in sync with components
npm run update-docs

# After adding a new component:
# 1. Add to src/index.ts exports
# 2. Update src/stories/prompts/General.stories.tsx
# 3. Run npm run update-docs to verify
# 4. Test downloadable JSON/MDX files
```

**Important**: The downloadable AI integration files must stay in sync with actual component exports and props. This ensures developers get accurate information when using AI tools like Lovable.dev, ChatGPT, etc.

## 📦 Publishing

### 🚀 Interactive CLI (Recommended)

Use the interactive publishing CLI for a guided experience:

```bash
npm run publish
```

### Quick Publishing Commands

```bash
# Validate package before publishing
npm run validate:package

# Create local package for testing
npm run publish:local

# Test publishing without actually publishing
npm run publish:dry-run

# Publish different version types
npm run publish:patch    # Bug fixes (1.0.1 → 1.0.2)
npm run publish:minor    # New features (1.0.1 → 1.1.0)
npm run publish:major    # Breaking changes (1.0.1 → 2.0.0)
npm run publish:beta     # Beta version for testing

# Publish to specific registries
npm run publish:github   # GitHub Packages (default)
npm run publish:npm      # Public npm registry
```

For detailed publishing instructions, see [📖 Publishing Guide](docs/PUBLISHING_GUIDE.md)

## 💖 Lovable.dev Integration

Your design system is **Lovable-ready**! Use it seamlessly in [Lovable.dev](https://lovable.dev) projects:

```bash
# In your Lovable project
npm install ft-design-system

# Generate templates (run in design system repo)
npm run lovable:templates
```

See [📖 Lovable Integration Guide](docs/LOVABLE_INTEGRATION_GUIDE.md) for complete setup instructions and examples.

## 🌐 Deployment & Sharing

### **For Designers**
This project includes comprehensive designer collaboration tools:
- 📖 See [`DESIGN_COLLABORATION.md`](DESIGN_COLLABORATION.md) for Storybook usage guide
- 🎨 Interactive component playground with real-time controls
- 📱 Responsive testing and design specifications

### **Automatic Deployment**
Deploy your Storybook automatically to Netlify:
- ✅ **Auto-deploy** on every GitHub push
- ✅ **Preview URLs** for pull requests  
- ✅ **Global CDN** for fast worldwide access
- ✅ **HTTPS** enabled by default

```bash
# One-command setup
npm run setup-deployment
```

See [`DEPLOYMENT_GUIDE.md`](DEPLOYMENT_GUIDE.md) for detailed deployment instructions.

## 📚 Documentation

Full documentation is available in Storybook. Run `npm run storybook` to view it locally.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting a pull request.

## 📄 License

MIT © [Chetan FT]

## 🙏 Acknowledgments

- Built with [Figma](https://figma.com)
- Powered by [React](https://reactjs.org)
- Styled with [Tailwind CSS](https://tailwindcss.com)
