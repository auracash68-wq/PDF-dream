package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ToolCategory(
    val id: String,
    val title: String,
    val shortName: String,
    val countLabel: String,
    val themeColor: Long,
    val chipBgColor: Long,
    val iconName: String
) {
    ORGANIZE(
        id = "cat-1",
        title = "1. Page Management & Organization",
        shortName = "Organize",
        countLabel = "15 Tools",
        themeColor = 0xFF2563EB,
        chipBgColor = 0xFFDBEAFE,
        iconName = "auto_stories"
    ),
    VIEWING(
        id = "cat-2",
        title = "2. Viewing, Rendering & Navigation",
        shortName = "View & Read",
        countLabel = "7 Tools",
        themeColor = 0xFF2563EB,
        chipBgColor = 0xFFDBEAFE,
        iconName = "visibility"
    ),
    MARKUP(
        id = "cat-3",
        title = "3. Vector Annotation, Markup & Drawing",
        shortName = "Markup",
        countLabel = "8 Tools",
        themeColor = 0xFF2563EB,
        chipBgColor = 0xFFDBEAFE,
        iconName = "draw"
    ),
    SCAN_CV(
        id = "cat-4",
        title = "4. On-Device Camera Scanner & CV",
        shortName = "Scan & CV",
        countLabel = "8 Tools",
        themeColor = 0xFF7C3AED,
        chipBgColor = 0xFFEDE9FE,
        iconName = "document_scanner"
    ),
    CONVERT(
        id = "cat-5",
        title = "5. Offline Conversion & Generation",
        shortName = "Convert",
        countLabel = "7 Tools",
        themeColor = 0xFFD97706,
        chipBgColor = 0xFFFEF3C7,
        iconName = "transform"
    ),
    SECURITY(
        id = "cat-6",
        title = "6. Security, Privacy & Metadata",
        shortName = "Security",
        countLabel = "8 Tools",
        themeColor = 0xFF115E59,
        chipBgColor = 0xFFCCFBF1,
        iconName = "shield_lock"
    ),
    FORMS_REPAIR(
        id = "cat-7",
        title = "7. Optimization, Repair & Forms",
        shortName = "Forms & Repair",
        countLabel = "12 Tools",
        themeColor = 0xFF0F766E,
        chipBgColor = 0xFFCCFBF1,
        iconName = "build_circle"
    )
}

data class PdfTool(
    val id: String,
    val name: String,
    val description: String,
    val actionBadge: String,
    val category: ToolCategory,
    val iconName: String,
    val isPinned: Boolean = false,
    val catTag: String = ""
)

object PdfToolRegistry {
    val ALL_TOOLS: List<PdfTool> = listOf(
        // Category 1: Page Management & Organization (15)
        PdfTool("merge", "Merge PDF", "Combine multiple documents into one seamless file", "Fast Merge", ToolCategory.ORGANIZE, "call_merge", true, "Cat 1"),
        PdfTool("split", "Split PDF", "Divide files by custom page ranges or single sheets", "Extract", ToolCategory.ORGANIZE, "call_split"),
        PdfTool("extract_pages", "Extract Pages", "Isolate and export designated pages quickly", "Export", ToolCategory.ORGANIZE, "tab_unselected"),
        PdfTool("delete_pages", "Delete Pages", "Remove unwanted pages and purge unused data", "Clean", ToolCategory.ORGANIZE, "delete_sweep"),
        PdfTool("rotate_pages", "Rotate Pages", "Turn pages 90°, 180°, or 270° orientation", "Rotate", ToolCategory.ORGANIZE, "rotate_right"),
        PdfTool("reorder_pages", "Reorder Pages", "Rearrange page sequence with visual drag & drop", "Organize", ToolCategory.ORGANIZE, "swap_vert"),
        PdfTool("crop_pdf", "Crop PDF", "Trim white margins & adjust precise page canvas", "Trim", ToolCategory.ORGANIZE, "crop"),
        PdfTool("halve_pages", "Halve PDF Pages", "Split dual book spreads into individual single pages", "Book Cut", ToolCategory.ORGANIZE, "view_column"),
        PdfTool("n_up", "N-Up Layout", "Place 2, 4, 9, or 16 pages onto a single sheet", "Multi-Grid", ToolCategory.ORGANIZE, "grid_4x4"),
        PdfTool("reverse_order", "Reverse Order", "Flip page order backwards in one instant step", "Invert", ToolCategory.ORGANIZE, "swap_driving_apps_wheel"),
        PdfTool("duplicate_pages", "Duplicate Pages", "Clone chosen pages anywhere within the document", "Clone", ToolCategory.ORGANIZE, "content_copy"),
        PdfTool("add_blank", "Add Blank Page", "Insert clean A4/Letter spacer pages at any spot", "Insert", ToolCategory.ORGANIZE, "note_add"),
        PdfTool("adjust_margins", "Adjust Margins", "Expand paper margins for binding and annotations", "Pad", ToolCategory.ORGANIZE, "aspect_ratio"),
        PdfTool("overlay_underlay", "Overlay / Underlay", "Layer letterheads, templates, or backgrounds", "Stamp", ToolCategory.ORGANIZE, "layers"),
        PdfTool("deskew_pages", "Deskew Pages", "Auto-straighten crooked scans with computer vision", "Align", ToolCategory.ORGANIZE, "straighten"),

        // Category 2: Viewing, Rendering & Navigation (7)
        PdfTool("multi_layout_viewer", "Multi-Layout Viewer", "Continuous scroll, single page, and 2-page reading modes", "Reader", ToolCategory.VIEWING, "chrome_reader_mode"),
        PdfTool("dark_mode_sepia", "Dark Mode & Sepia", "Comfortable night reading with OLED black & sepia tone", "Display", ToolCategory.VIEWING, "dark_mode"),
        PdfTool("full_text_search", "Full-Text Search", "Lightning-fast keyword and regex search with instant hits", "Search", ToolCategory.VIEWING, "find_in_page"),
        PdfTool("bookmark_editor", "Bookmark Editor", "Build and structure tree outlines for easy document access", "Outlines", ToolCategory.VIEWING, "bookmarks"),
        PdfTool("toc_navigator", "TOC Navigator", "Instant jump to chapters and interactive link targets", "Index", ToolCategory.VIEWING, "toc"),
        PdfTool("tts_audio", "Offline Text-to-Speech", "Listen to PDF contents using native device voice synthesis", "Audio", ToolCategory.VIEWING, "volume_up"),
        PdfTool("thumbnail_grid", "Thumbnail Grid", "Bird's-eye preview of hundreds of pages at once", "Preview", ToolCategory.VIEWING, "grid_view"),

        // Category 3: Vector Annotation, Markup & Drawing (8)
        PdfTool("text_highlighter", "Text Highlighter", "Highlight exact text strings with vibrant translucent inks", "Markup", ToolCategory.MARKUP, "ink_highlighter"),
        PdfTool("underline_strike", "Underline / Strike", "Vector proofreading marks and corrections", "Proofread", ToolCategory.MARKUP, "format_underlined"),
        PdfTool("freehand_pen", "Freehand Pen", "Pressure-sensitive Apple Pencil & stylus vector strokes", "Draw", ToolCategory.MARKUP, "edit"),
        PdfTool("area_highlighter", "Area Highlighter", "Highlight box regions across diagrams and scanned images", "Region", ToolCategory.MARKUP, "highlight_alt"),
        PdfTool("sticky_notes", "Sticky Notes", "Expandable comments and directional pointer callouts", "Notes", ToolCategory.MARKUP, "sticky_note_2"),
        PdfTool("geometric_shapes", "Geometric Shapes", "Crisp rectangles, arrows, circles, and polygons", "Shapes", ToolCategory.MARKUP, "shapes"),
        PdfTool("stamps_badges", "Stamps & Badges", "APPROVED, CONFIDENTIAL, and custom stamp markers", "Stamps", ToolCategory.MARKUP, "approval"),
        PdfTool("measurement_tools", "Measurement Tools", "Calculate scale distances, perimeter, and blueprint areas", "Measure", ToolCategory.MARKUP, "square_foot"),

        // Category 4: On-Device Camera Scanner & CV (8)
        PdfTool("camera_scan", "Camera CV Scan", "Auto-edge & deskew with boundary snap", "AI Auto", ToolCategory.SCAN_CV, "document_scanner", true, "Cat 4"),
        PdfTool("perspective_fix", "Perspective Fix", "Rectify angled handheld shots into flat orthographic views", "Unwarp", ToolCategory.SCAN_CV, "transform"),
        PdfTool("shadow_erase", "Shadow Erase", "Remove finger shadows and lighting glare adaptively", "Clean", ToolCategory.SCAN_CV, "flare"),
        PdfTool("magic_color", "Magic Color Boost", "Boost contrast and dynamic range for crisp documents", "Enhance", ToolCategory.SCAN_CV, "auto_fix_high"),
        PdfTool("bw_filter", "B&W Document Filter", "High-contrast binary scan mode for ultra-tiny file sizes", "Otsu B&W", ToolCategory.SCAN_CV, "filter_b_and_w"),
        PdfTool("id_passport", "ID & Passport 2-in-1", "Combines front & back cards onto a single print-ready page", "Layout", ToolCategory.SCAN_CV, "badge"),
        PdfTool("book_spine_flattener", "Book Spine Flattener", "Corrects 3D curved surfaces into flat readable text", "3D Mesh", ToolCategory.SCAN_CV, "menu_book"),
        PdfTool("offline_ocr", "Offline OCR", "Neural on-device optical character recognition to searchable PDF", "On-device", ToolCategory.SCAN_CV, "font_download", true, "Cat 4"),

        // Category 5: Offline Conversion & Generation (7)
        PdfTool("images_to_pdf", "Images to PDF", "Compile JPG, PNG, HEIC, and WEBP photos to PDF", "Compile", ToolCategory.CONVERT, "photo_library"),
        PdfTool("pdf_to_images", "PDF to Images", "Export high-resolution raster images page by page", "Render", ToolCategory.CONVERT, "image"),
        PdfTool("extract_images", "Extract Images", "Extract original uncompressed raw graphics and photos", "Rip", ToolCategory.CONVERT, "collections_bookmark"),
        PdfTool("txt_to_pdf", "TXT to PDF", "Turn plain text notes into styled paginated documents", "Format", ToolCategory.CONVERT, "description"),
        PdfTool("markdown_to_pdf", "Markdown to PDF", "Render styled .md with typography, tables & code blocks", "Syntax", ToolCategory.CONVERT, "code_blocks"),
        PdfTool("qr_barcode_gen", "QR & Barcode Generator", "Generate offline 1D/2D machine barcodes into print sheets", "Codes", ToolCategory.CONVERT, "qr_code_2"),
        PdfTool("camera_to_pdf", "Camera to PDF", "Live multi-page continuous hardware camera capture", "Direct", ToolCategory.CONVERT, "photo_camera"),

        // Category 6: Security, Privacy & Metadata (8)
        PdfTool("password_protect", "Protect & Encrypt", "Lock files with military-grade AES-256 bit encryption", "AES-256", ToolCategory.SECURITY, "lock", true, "Cat 6"),
        PdfTool("unlock_pdf", "Unlock PDF", "Remove protection & print restrictions permanently", "Decrypt", ToolCategory.SECURITY, "lock_open"),
        PdfTool("permanent_redaction", "Permanent Redaction", "Permanently blackout and purge confidential vector text", "Scrub", ToolCategory.SECURITY, "ink_eraser"),
        PdfTool("metadata_stripper", "Metadata Stripper", "Purge author, camera GPS, and hidden XMP trails", "Privacy", ToolCategory.SECURITY, "info"),
        PdfTool("digital_cert_sign", "Digital Certificate Sign", "Sign with .pfx/.p12 cryptographic PKCS#12 certs", "PKCS#12", ToolCategory.SECURITY, "verified_user"),
        PdfTool("drawn_signature", "E-Signature", "Draw, store, and stamp your personal handwritten sign", "Legal", ToolCategory.SECURITY, "signature", true, "Cat 6"),
        PdfTool("remove_scripts", "Remove Scripts", "Neutralize embedded /JS macros and potential exploits", "Defend", ToolCategory.SECURITY, "security"),
        PdfTool("attachment_manager", "Attachment Manager", "Add or extract embedded external file attachments", "Files", ToolCategory.SECURITY, "attachment"),

        // Category 7: Optimization, Repair & Forms (12)
        PdfTool("pdf_compression", "Compress PDF", "Reduce MBs with smart image downsampling & Flate", "Custom DPI", ToolCategory.FORMS_REPAIR, "compress", true, "Cat 7"),
        PdfTool("flatten_pdf", "Flatten PDF", "Convert form fields and markups into static page content", "Static", ToolCategory.FORMS_REPAIR, "layers_clear"),
        PdfTool("repair_pdf", "Corrupted PDF Repair", "Rebuild broken cross-reference tables and recover pages", "Salvage", ToolCategory.FORMS_REPAIR, "healing"),
        PdfTool("bates_numbering", "Bates Numbering", "Apply sequential legal Bates IDs and custom pagination", "Legal", ToolCategory.FORMS_REPAIR, "tag"),
        PdfTool("add_watermark", "Add Watermark", "Custom text or logo watermarks with opacity and angle", "Protect", ToolCategory.FORMS_REPAIR, "branding_watermark"),
        PdfTool("pdfa_archival", "PDF/A Archival", "ISO 19005 standard compliance for long-term storage", "ISO Standard", ToolCategory.FORMS_REPAIR, "inventory_2"),
        PdfTool("scanner_look", "Scanner Look Effect", "Realistic photocopy grain, slight skew & paper warmth", "Vintage", ToolCategory.FORMS_REPAIR, "grain"),
        PdfTool("form_filler", "Form Filler", "Fill interactive AcroForms, radio buttons & checkboxes", "AcroForms", ToolCategory.FORMS_REPAIR, "dynamic_form"),
        PdfTool("form_data_export", "Form Data Export", "Import and export values to CSV, FDF, and XFDF formats", "CSV/FDF", ToolCategory.FORMS_REPAIR, "dataset"),
        PdfTool("fast_web_view", "Fast Web View", "Linearize documents for instant first-page byte serving", "Fast Stream", ToolCategory.FORMS_REPAIR, "speed"),
        PdfTool("font_embedding", "Font Embedding", "Embed TrueType & OpenType font subsets to avoid font missing", "Typography", ToolCategory.FORMS_REPAIR, "text_fields"),
        PdfTool("cmyk_rgb_convert", "CMYK / RGB Convert", "Pre-press ICC color profiles and device calibration", "ICC Profile", ToolCategory.FORMS_REPAIR, "palette")
    )
}
