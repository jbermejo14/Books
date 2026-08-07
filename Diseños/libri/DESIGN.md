---
name: Libri
colors:
  surface: '#fbf9f8'
  surface-dim: '#dbd9d9'
  surface-bright: '#fbf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#efeded'
  surface-container-high: '#eae8e7'
  surface-container-highest: '#e4e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#43474d'
  inverse-surface: '#303030'
  inverse-on-surface: '#f2f0f0'
  outline: '#74777d'
  outline-variant: '#c4c6cd'
  surface-tint: '#4c6078'
  primary: '#03192e'
  on-primary: '#ffffff'
  primary-container: '#1a2e44'
  on-primary-container: '#8296b0'
  inverse-primary: '#b4c8e4'
  secondary: '#7d562d'
  on-secondary: '#ffffff'
  secondary-container: '#ffca98'
  on-secondary-container: '#7a532a'
  tertiary: '#181815'
  on-tertiary: '#ffffff'
  tertiary-container: '#2d2d29'
  on-tertiary-container: '#96948e'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d1e4ff'
  primary-fixed-dim: '#b4c8e4'
  on-primary-fixed: '#061d32'
  on-primary-fixed-variant: '#35485f'
  secondary-fixed: '#ffdcbd'
  secondary-fixed-dim: '#f0bd8b'
  on-secondary-fixed: '#2c1600'
  on-secondary-fixed-variant: '#623f18'
  tertiary-fixed: '#e5e2db'
  tertiary-fixed-dim: '#c9c6c0'
  on-tertiary-fixed: '#1c1c18'
  on-tertiary-fixed-variant: '#474742'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e2'
typography:
  display-lg:
    fontFamily: Libre Caslon Text
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Libre Caslon Text
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Libre Caslon Text
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Libre Caslon Text
    fontSize: 24px
    fontWeight: '500'
    lineHeight: 32px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 48px
  xl: 80px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 64px
---

## Brand & Style
The design system for this application is centered on the concept of a "Digital Sanctuary." It targets bibliophiles who appreciate the tactile and intellectual comfort of a physical library but require modern organizational efficiency. The aesthetic is a refined blend of **Minimalism** and **Tactile/Skeuomorphism**, prioritizing high-quality white space and subtle depth to mimic the layering of paper and wood. The emotional response should be one of calm, focus, and quiet sophistication.

## Colors
The palette is inspired by the materials of traditional bookmaking.
- **Primary (Ink Blue):** Used for primary text, deep branding moments, and active navigation states. It represents the permanence of printed word.
- **Secondary (Warm Amber):** Used sparingly for accents, call-to-action buttons, and progress indicators. It evokes the warmth of polished wood or reading lamps.
- **Tertiary (Paper White):** The core background color. A soft, off-white that reduces eye strain and provides a parchment-like quality.
- **Neutral (Charcoal):** Used for secondary text, metadata, and borders to maintain high legibility without the harshness of pure black.

## Typography
This design system utilizes a high-contrast typographic pairing to bridge the gap between editorial tradition and modern utility. **Libre Caslon Text** is used for book titles, section headers, and "hero" moments to provide an authoritative, literary feel. **Manrope** is used for all functional UI elements, body copy, and metadata to ensure maximum readability and a clean, modern interface. All labels should use slightly increased letter spacing when in uppercase to maintain a sophisticated architectural look.

## Layout & Spacing
The layout follows a **Fixed Grid** philosophy on desktop to create a sense of a "contained volume," while transitioning to a fluid model on mobile. 
- **Desktop:** 12-column grid with a max-width of 1280px. Large margins (64px) help center the focus, mimicking the margins of a well-designed book page.
- **Mobile:** 4-column fluid grid with 16px margins. 
Vertical rhythm is strictly maintained using multiples of 8px to ensure a structured, harmonious flow of information. Padding within cards and containers should be generous (24px+) to prevent the UI from feeling cluttered.

## Elevation & Depth
Depth is communicated through **Tonal Layers** and **Ambient Shadows**. Surfaces should feel like stacked sheets of heavy-weight paper.
- **Level 0 (Background):** The Paper White base.
- **Level 1 (Cards/Lists):** Slightly raised with an extremely soft, diffused shadow (Blur: 16px, Y: 4px, Opacity: 4% Ink Blue).
- **Level 2 (Modals/Overlays):** Significant lift with a larger shadow and a subtle 1px border in a slightly darker paper tone to define edges.
Avoid harsh blacks in shadows; always tint shadows with the primary Ink Blue to maintain the "cozy" atmosphere.

## Shapes
The shape language is **Rounded**, avoiding sharp institutional corners in favor of softer, organic lines. 
- **Standard Elements:** 0.5rem (8px) radius for buttons and input fields.
- **Containers:** 1rem (16px) for cards and main content areas.
- **Interactive Tags:** Use the `rounded-xl` (24px) setting for a pill-shaped appearance to distinguish them from functional buttons.

## Components
- **Buttons:** Primary buttons use the Ink Blue background with Paper White text. Secondary buttons use a transparent background with a 1.5px Ink Blue border. "Favorite" or "Action" buttons may use the Warm Amber accent.
- **Input Fields:** Minimalist style with only a bottom border of 1px in a neutral tone, which thickens and changes to Ink Blue on focus. 
- **Cards:** Book cards should prioritize the cover art. Text is placed on a Paper White container below or overlapping the bottom edge.
- **Chips/Tags:** Used for genres or moods, these should have a subtle Paper White fill and a thin neutral border to feel like small bookmarks.
- **Icons:** Use 1.5pt stroke weight, "Light" or "Thin" icons. Avoid filled icons unless indicating an active toggle state (e.g., a filled heart for "Favorited").
- **Progress Bars:** Reading progress should be shown using a thin, Warm Amber line against a very light neutral track, appearing elegant rather than "gamified."