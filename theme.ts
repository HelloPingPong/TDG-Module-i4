/**
 * Theme configuration for TDG
 * This file provides TypeScript interfaces and constants for the design system
 */

// Color palette
export const colors = {
  // Primary colors
  primary: {
    main: 'var(--jpm-primary)',
    dark: 'var(--jpm-primary-dark)',
    light: 'var(--jpm-primary-light)',
  },
  
  // Secondary and accent colors
  secondary: 'var(--jpm-secondary)',
  accent: 'var(--jpm-accent)',
  warning: 'var(--jpm-warning)',
  error: 'var(--jpm-error)',
  info: 'var(--jpm-info)',
  
  // Neutral colors
  neutral: {
    100: 'var(--jpm-neutral-100)',
    200: 'var(--jpm-neutral-200)',
    300: 'var(--jpm-neutral-300)',
    400: 'var(--jpm-neutral-400)',
    500: 'var(--jpm-neutral-500)',
    600: 'var(--jpm-neutral-600)',
    700: 'var(--jpm-neutral-700)',
    800: 'var(--jpm-neutral-800)',
    900: 'var(--jpm-neutral-900)',
  },
};

// Typography configuration
export const typography = {
  fontFamily: {
    base: 'var(--jpm-font-family)',
    heading: 'var(--jpm-heading-font-family)',
    mono: 'var(--jpm-mono-font-family)',
  },
  
  fontSize: {
    xs: 'var(--jpm-font-size-xs)',     // 12px
    sm: 'var(--jpm-font-size-sm)',     // 14px
    md: 'var(--jpm-font-size-md)',     // 16px
    lg: 'var(--jpm-font-size-lg)',     // 18px
    xl: 'var(--jpm-font-size-xl)',     // 20px
    '2xl': 'var(--jpm-font-size-2xl)', // 24px
    '3xl': 'var(--jpm-font-size-3xl)', // 30px
    '4xl': 'var(--jpm-font-size-4xl)', // 36px
  },
  
  fontWeight: {
    light: 'var(--jpm-font-weight-light)',
    regular: 'var(--jpm-font-weight-regular)',
    medium: 'var(--jpm-font-weight-medium)',
    semibold: 'var(--jpm-font-weight-semibold)',
    bold: 'var(--jpm-font-weight-bold)',
  },
  
  lineHeight: {
    tight: 'var(--jpm-line-height-tight)',
    normal: 'var(--jpm-line-height-normal)',
    relaxed: 'var(--jpm-line-height-relaxed)',
  },
};

// Spacing system
export const spacing = {
  1: 'var(--jpm-space-1)',   // 4px
  2: 'var(--jpm-space-2)',   // 8px
  3: 'var(--jpm-space-3)',   // 12px
  4: 'var(--jpm-space-4)',   // 16px
  5: 'var(--jpm-space-5)',   // 24px
  6: 'var(--jpm-space-6)',   // 32px
  8: 'var(--jpm-space-8)',   // 48px
  10: 'var(--jpm-space-10)', // 64px
  12: 'var(--jpm-space-12)', // 80px
};

// Border radius
export const borderRadius = {
  sm: 'var(--jpm-radius-sm)',
  md: 'var(--jpm-radius-md)',
  lg: 'var(--jpm-radius-lg)',
  xl: 'var(--jpm-radius-xl)',
  full: 'var(--jpm-radius-full)',
};

// Shadows
export const shadows = {
  sm: 'var(--jpm-shadow-sm)',
  md: 'var(--jpm-shadow-md)',
  lg: 'var(--jpm-shadow-lg)',
  xl: 'var(--jpm-shadow-xl)',
};

// Transitions
export const transitions = {
  normal: 'var(--jpm-transition-normal)',
  slow: 'var(--jpm-transition-slow)',
  fast: 'var(--jpm-transition-fast)',
};

// Z-index layers
export const zIndex = {
  base: 'var(--jpm-z-base)',
  elevated: 'var(--jpm-z-elevated)',
  dropdown: 'var(--jpm-z-dropdown)',
  sticky: 'var(--jpm-z-sticky)',
  fixed: 'var(--jpm-z-fixed)',
  modal: 'var(--jpm-z-modal)',
  popover: 'var(--jpm-z-popover)',
  toast: 'var(--jpm-z-toast)',
  tooltip: 'var(--jpm-z-tooltip)',
};

// Breakpoints for responsive design
export const breakpoints = {
  xs: '0px',
  sm: '576px',
  md: '768px',
  lg: '992px',
  xl: '1200px',
  xxl: '1400px',
};

// Component-specific themes
export const components = {
  // Button variants
  button: {
    variants: {
      primary: {
        backgroundColor: colors.primary.main,
        color: colors.neutral[100],
        hoverBackgroundColor: colors.primary.dark,
      },
      secondary: {
        backgroundColor: colors.secondary,
        color: colors.neutral[100],
        hoverBackgroundColor: colors.neutral[700],
      },
      outline: {
        backgroundColor: 'transparent',
        color: colors.primary.main,
        borderColor: colors.primary.main,
        hoverBackgroundColor: colors.primary.light,
      },
      text: {
        backgroundColor: 'transparent',
        color: colors.primary.main,
        hoverBackgroundColor: colors.neutral[200],
      },
      danger: {
        backgroundColor: colors.error,
        color: colors.neutral[100],
        hoverBackgroundColor: '#b32d01', // Darker red
      },
      success: {
        backgroundColor: colors.accent,
        color: colors.neutral[100],
        hoverBackgroundColor: '#0e6a0e', // Darker green
      },
    },
    sizes: {
      sm: {
        padding: `${spacing[1]} ${spacing[2]}`,
        fontSize: typography.fontSize.sm,
        minHeight: '2rem',
      },
      md: {
        padding: `${spacing[2]} ${spacing[4]}`,
        fontSize: typography.fontSize.md,
        minHeight: '2.5rem',
      },
      lg: {
        padding: `${spacing[3]} ${spacing[5]}`,
        fontSize: typography.fontSize.lg,
        minHeight: '3rem',
      },
    },
  },
  
  // Card component
  card: {
    backgroundColor: colors.neutral[100],
    borderRadius: borderRadius.md,
    boxShadow: shadows.md,
    
    header: {
      padding: spacing[4],
      borderBottom: `1px solid ${colors.neutral[300]}`,
      backgroundColor: colors.neutral[200],
    },
    body: {
      padding: spacing[4],
    },
    footer: {
      padding: spacing[4],
      borderTop: `1px solid ${colors.neutral[300]}`,
      backgroundColor: colors.neutral[200],
    },
  },
  
  // Input component
  input: {
    padding: `${spacing[2]} ${spacing[3]}`,
    borderColor: colors.neutral[300],
    borderRadius: borderRadius.md,
    backgroundColor: colors.neutral[100],
    
    focus: {
      borderColor: colors.primary.main,
      boxShadow: `0 0 0 3px ${colors.primary.light}`,
    },
    
    error: {
      borderColor: colors.error,
      boxShadow: '0 0 0 3px rgba(216, 59, 1, 0.2)',
    },
  },
  
  // Table component
  table: {
    headerBackgroundColor: colors.neutral[200],
    borderColor: colors.neutral[300],
    hoverBackgroundColor: colors.neutral[200],
  },
};

// The complete theme object
const theme = {
  colors,
  typography,
  spacing,
  borderRadius,
  shadows,
  transitions,
  zIndex,
  breakpoints,
  components,
};

export default theme;

// Theme TypeScript interfaces
export interface ThemeColors {
  primary: {
    main: string;
    dark: string;
    light: string;
  };
  secondary: string;
  accent: string;
  warning: string;
  error: string;
  info: string;
  neutral: {
    [key: number]: string;
  };
}

export interface ThemeTypography {
  fontFamily: {
    base: string;
    heading: string;
    mono: string;
  };
  fontSize: {
    [key: string]: string;
  };
  fontWeight: {
    light: string;
    regular: string;
    medium: string;
    semibold: string;
    bold: string;
  };
  lineHeight: {
    tight: string;
    normal: string;
    relaxed: string;
  };
}

export interface Theme {
  colors: ThemeColors;
  typography: ThemeTypography;
  spacing: {
    [key: number]: string;
  };
  borderRadius: {
    [key: string]: string;
  };
  shadows: {
    [key: string]: string;
  };
  transitions: {
    [key: string]: string;
  };
  zIndex: {
    [key: string]: string;
  };
  breakpoints: {
    [key: string]: string;
  };
  components: {
    [key: string]: any;
  };
}

// Helper function to get values from theme by path
export function getThemeValue(path: string, defaultValue?: any): any {
  const parts = path.split('.');
  let value: any = theme;
  
  for (const part of parts) {
    if (value === undefined) return defaultValue;
    value = value[part];
  }
  
  return value !== undefined ? value : defaultValue;
}
