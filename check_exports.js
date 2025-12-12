const ftds = require('./node_modules/ft-design-system/dist/index.js');
const components = Object.keys(ftds)
  .filter(key => {
    const item = ftds[key];
    return (typeof item === 'function' || typeof item === 'object') && 
           key[0] === key[0].toUpperCase() &&
           !key.includes('Props') &&
           !key.includes('Type') &&
           !key.includes('Config') &&
           !key.includes('Provider') &&
           !key.includes('Context');
  })
  .sort();

console.log('=== AVAILABLE COMPONENTS ===');
console.log(components.join(', '));
console.log('\n=== TOTAL COUNT:', components.length, '===');
