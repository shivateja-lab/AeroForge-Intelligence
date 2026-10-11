# AeroForge Intelligence — MVP Status

## Completed

- JWT authentication with BCrypt password storage
- User roles and stateless Spring Security
- Material-batch registration and listing
- TIG weld-parameter capture and calculated heat input
- Property prediction for tensile strength, yield strength and elongation
- Defect probability, confidence score and LOW/MEDIUM/HIGH risk classification
- Automatic critical alert creation for high-risk predictions
- TiDB persistence through the existing Flyway schema
- Responsive React engineering dashboard
- API validation and structured errors
- Git ignore rules, Docker Compose and GitHub-ready documentation

## Verification

- `npm run lint`: passed
- `npm run build`: passed
- Spring source/configuration inspected for the Java 21 / Spring Boot 4 project
- Backend dependency download could not be repeated in the isolated build workspace because Maven Central access is blocked there. Run `./mvnw clean test` on the development Mac, where the project dependencies are already available.

## Engineering limitation

The included predictor is a transparent physics-informed baseline suitable for workflow demonstration. It is not trained or certified on flight data. Production use requires authorized experimental datasets, model validation, traceability, calibration and review by qualified aerospace material engineers.
