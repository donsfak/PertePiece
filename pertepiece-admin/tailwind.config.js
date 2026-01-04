/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#e6f0ed',
          100: '#c2dbd4',
          200: '#9ac5b8',
          300: '#72af9c',
          400: '#539d87',
          500: '#0F4C3A',
          600: '#0d4434',
          700: '#0a3a2c',
          800: '#083024',
          900: '#05201a',
        },
        accent: {
          50: '#fff2e6',
          100: '#ffdfbf',
          200: '#ffca95',
          300: '#ffb56b',
          400: '#ffa64b',
          500: '#FF6B00',
          600: '#e66100',
          700: '#cc5600',
          800: '#b34c00',
          900: '#993f00',
        },
        background: '#F8FAFC',
        surface: '#FFFFFF',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
