# ID age check

SimClient has three bottom tabs: **Product Editing**, **Stock Moving**, and **Camera**. The third tab uses a camera icon. Each tap on **Camera** opens the phone's camera, including when that tab is already selected. Photograph the front of a Québec driver's licence, RAMQ health insurance card, or Canadian permanent resident card. Bundled ML Kit Latin OCR reads English/French text offline; no server or API key is required. Settings and Refresh use labelled-for-accessibility gear and circular-arrow icons.

Successful results show only **Birth Date** and a large age number, calculated in completed years using today's date in `America/Toronto`. A unique birth-labelled date is preferred; otherwise a single remaining date is used. Ambiguous or unreadable dates, cancelled captures, and camera failures show Year / Month / Day inputs. Entering a four-digit year immediately shows the possible age range; adding month and day narrows it to an exact age. Invalid or future dates clear the previous result and show an input error. Tap the edit icon beside a recognized birth date to correct it. There are no confirmation dialogs. Tap **Camera** again to clear the previous result and retake. Returning from the camera does not reopen it automatically. No adult/underage approval or ID-authenticity decision is made.

## Extraction

- Recognizes English/French birth labels, numeric dates with four-digit years, and English/French month names and common Canadian abbreviations.
- Excludes lines labelled as issue/expiry dates. Multiple unresolved dates offer manual entry or a retake.
- Keeps both interpretations of ambiguous day/month dates. Rejects impossible dates, future dates, and dates older than 120 years.
- Does not infer centuries, repair OCR digits, decode RAMQ numbers, or read barcodes/MRZ. Other card layouts may require manual entry.
- Uses completed calendar years; a February 29 birthday advances on March 1 in a non-leap year.

## Data lifecycle

The full-size camera image goes to a narrowly scoped FileProvider cache directory. It is decoded off the UI thread, downsampled and corrected for EXIF orientation. OCR runs on-device. The app deletes its temporary capture and revokes URI grants on completion/cancellation. Files left by process termination are removed after 24 hours on the next visit. The external camera app controls any copies it independently retains.

OCR text and DOB are never logged, persisted, or sent to the POS. Results are not restored after process death. The scan screen disables screenshots and recent-app previews. It uses the external camera app, so no new camera/storage permission is needed.

## Validation

`tools/checks/PartialBirthDateCheck.java` covers partial-date ranges, invalid dates, and birthday boundaries. `tools/checks/BirthDateParserCheck.java` is a standalone Java check for parsing, ambiguity, expiry exclusion, invalid dates, birthday boundaries, and leap days. Compile it together with `BirthDateParser.java` and run `BirthDateParserCheck`.

On a phone, check: offline capture, camera cancellation, blurry/rotated images, English/French dates, several dates on one card, repeated taps of the selected ID tab clearing the previous result, background/return from camera without a relaunch loop, and no-camera handling. Actual card OCR accuracy requires representative device testing; parser checks alone do not establish recognition accuracy.

References:
- https://developers.google.com/ml-kit/vision/text-recognition/v2/android
- https://saaq.gouv.qc.ca/blob/saaq/documents/publications/presentation-quebec-driver-licences.pdf
- https://www.ramq.gouv.qc.ca/en/citizens/health-insurance/using-card

Product-photo lookup now shares the camera. See [camera-products.md](camera-products.md) for routing and product matching. Failed or unclassified photos offer product search and an Enter birth date action.
