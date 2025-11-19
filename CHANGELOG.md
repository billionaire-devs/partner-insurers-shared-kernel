## [2.0.0](https://github.com/billionaire-devs/partner-insurers-shared-kernel/compare/v1.3.0...v2.0.0) (2025-11-19)

### ⚠ BREAKING CHANGES

* ApiResponse error details changed from JSON string to Map<String, String>

Changes include:

Presentation Layer Tests:
- GlobalExceptionHandlerTest: Validates exception-to-HTTP mapping with proper
  status codes, error codes, and metadata enrichment for ValidationException,
  ConstraintViolationException, MissingServletRequestParameterException, and
  IllegalArgumentException
- ApiResponseBodyAdviceTest: Verifies conditional response wrapping based on
  configuration, media type filtering, and proper ApiResponse structure
- ApiResponseSerializationTest: Updated to use Map for error details instead
  of JSON string, improving type safety

Domain Value Objects Tests:
- AddressTest: Validates address creation, validation rules, and equality
- DomainEntityIdTest: Tests UUID-based entity ID generation and validation
- EmailTest: Verifies email format validation and normalization
- PhoneTest: Tests phone number validation with international format support
- UrlTest: Validates URL format and protocol requirements

Infrastructure Components:
- ValidationError: New domain model for structured validation error representation
- SharedKernelPresentationProperties: Configuration properties for API metadata
  defaults (version, environment)
- SharedKernelRequestTimingFilter: Request timing filter for performance tracking

Test Improvements:
- All tests use MockHttpServletRequest for realistic HTTP context
- Proper assertion of error codes, messages, and structured details
- Validation of metadata enrichment (correlation ID, timing, environment)
- HTML content preservation verified (no escaping in error messages)
- ConstraintViolation mock implementation for Jakarta validation testing

The error details structure change from JSON string to Map improves:
- Type safety and compile-time validation
- Easier programmatic access to error fields
- Better serialization control and consistency

### Tests

* add comprehensive test coverage for shared kernel presentation layer ([cf3d165](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/cf3d165fe8fba3fd60bde555a473d992dfb28183))

## [1.3.0](https://github.com/billionaire-devs/partner-insurers-shared-kernel/compare/v1.2.0...v1.3.0) (2025-11-13)

### Features

* add soft delete functionality to Model base class ([7161ae2](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/7161ae2e54e227e1a8b608511beb56823fb4d496))

## [1.2.0](https://github.com/billionaire-devs/partner-insurers-shared-kernel/compare/v1.1.0...v1.2.0) (2025-11-13)

### Features

* add exception handlers for FailedToUpdateEntityException and NoSuchElementException ([79ffeb7](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/79ffeb7a587451e9fa73e17b400b9282dfd1c2a2))

## [1.1.0](https://github.com/billionaire-devs/partner-insurers-shared-kernel/compare/v1.0.0...v1.1.0) (2025-11-11)

### Features

* add query view enum for level of details ([1e1059a](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/1e1059afaccdab7d79984dec456c8215ed869f6c))

## 1.0.0 (2025-11-09)

### Features

* configure as Spring Boot auto-configuration library ([8fe6f0a](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/8fe6f0a9337bb884b5398604661afccc6fab7405))
* externalize build metadata and enrich API responses ([b1c530e](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/b1c530e56af1e6253cbb118a39cd64a7f57f2fbe))

### Bug Fixes

* add conventional-changelog-conventionalcommits to resolve package not found ([2c44278](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/2c44278225ade5e60df9cea2f93dafdac6f9612b))
* register global exception handler and cover illegal state ([1756f43](https://github.com/billionaire-devs/partner-insurers-shared-kernel/commit/1756f435f6232de9e1d3f92e04a4602ae118f93e))
