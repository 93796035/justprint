# Camera: product lookup and ID age check

The third bottom tab is now a camera icon. Every tap starts a fresh capture. OCR remains local and uses the same bundled model as the ID feature.

The capture screen is owned by SimClient through CameraX. Pressing its shutter saves the photo and returns directly to recognition; there is no system-camera `Use photo / Retake` review screen. The first use may still show Android's required camera-permission prompt. Cancel closes the camera without processing a photo.

For product photos, the client normalizes case, accents, punctuation, and line breaks (including `COLT45` versus `COLT 45`). Common packaging terms and explicit sizes are excluded from the brand identity. Product names are matched against the locally downloaded catalog; current-category matches take priority, using the same parent-category grouping as product editing. If that category has no candidates, the entire catalog is searched.

- One complete name-identity match and no competing candidates: switch to Product Editing and fill that product.
- Multiple candidates, or a partial/fuzzy match: show a scrollable chooser with product name, category and SKU/ID. Different capacities/packs remain separate candidates.
- No candidates: offer a manual name search or retake. An empty catalog needs Refresh.

Matching never submits inventory changes. Choosing a candidate only opens the existing editing flow. OCR cleanup finishes before navigation, so the busy-state guard does not block the product selection.

Birth-labelled text or ID-specific headings continue into the age flow. An unlabelled date on a beer can does not automatically become a birth date. Recovery also offers Enter birth date, retaining the progressive year/month/day age calculation. When a candidate list was shown for an ID, use This is an ID to run the existing date parser.

Photos are routed as one of two supported subjects. A decoded retail barcode, catalog-name match, or strong beer-label wording routes to Product Editing; if no product matches, the barcode or best label text is placed in the product search field. ID headings or a labelled birth date route to age checking. If the type is uncertain or OCR fails, the app defaults to the manual year/month/day ID screen, with a `This is beer — search products` action for correction.

`tools/checks/PhotoProductMatcherCheck.java` exercises category priority, full-catalog fallback, Colt 45, package variants, accents, OCR typos, and rejecting generic/numeric-only matches. Compile with `PhotoProductMatcher.java` and run the check. Actual OCR on curved, reflective cans still needs representative phone photos; retake or manual search is available when the label is unclear.

When automatic product selection fails, the camera page displays the original OCR text (including line breaks). It distinguishes an empty OCR result, a missing local catalog, no catalog matches, and uncertain candidates. The text is selectable, remains only in the current view, and is not persisted or uploaded. OCR engine failures remain separate from successful OCR with no text. Manual search remains available underneath.

## Barcode lookup

The camera now first decodes EAN-13, EAN-8, UPC-A, UPC-E, ITF and numeric Code 128 with bundled offline ML Kit barcode scanning (`com.google.mlkit:barcode-scanning:17.3.0`; https://developers.google.com/ml-kit/vision/barcode-scanning/android). If decoding returns no numeric code, the existing OCR/ID path runs; printed digit sequences are tried before product names. Barcode matches use the catalog CODE field, not product names.

Full codes take precedence across all categories, including equivalent UPC-A / zero-prefixed EAN-13. A unique decoded full-code match opens Product Editing. OCR digits, duplicate codes, multiple scanned codes, and incomplete codes require confirmation. Candidates in the selected category appear first without hiding other matches. Partial matching requires at least six consecutive digits, no substitutions, and at most two missing digits on either end (three total). Short fragments and dates are not accepted as barcode queries. Recognized codes/text remain visible on failure, with manual barcode/name search available. Test: tools/checks/BarcodeProductMatcherCheck.java. Real camera performance still depends on focus, glare, and the barcode being present in the catalog.
