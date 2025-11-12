package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.request.*;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.ClassProperty;
import kr.com.mfa.mfaphase1api.model.enums.ClassSubSubjectProperty;
import kr.com.mfa.mfaphase1api.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class ClassController {

    private final ClassService classService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create class",
            description = "Creates a new class. Name must be unique per scope (case-insensitive).",
            tags = {"Class"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = ClassResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Class already exists")
            }
    )
    public ResponseEntity<APIResponse<ClassResponse>> createClass(
            @RequestBody @Valid ClassRequest request
    ) {
        return buildResponse("Class created", classService.createClass(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List classes",
            description = "Returns a paginated list of classes with sorting.",
            tags = {"Class"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<ClassResponse>>>> getAllClasses(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "NAME")
            @RequestParam(required = false, defaultValue = "NAME") ClassProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Classes retrieved",
                classService.getAllClasses(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{classId}")
    @Operation(
            summary = "Get class",
            description = "Returns a class by its ID.",
            tags = {"Class"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = ClassResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Class not found")
            }
    )
    public ResponseEntity<APIResponse<ClassResponse>> getClassById(
            @PathVariable UUID classId
    ) {
        return buildResponse("Class retrieved", classService.getClassById(classId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}")
    @Operation(
            summary = "Update class",
            description = "Updates a class by ID. Name must remain unique within the same scope.",
            tags = {"Class"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = ClassResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Class not found"),
                    @ApiResponse(responseCode = "409", description = "Class already exists")
            }
    )
    public ResponseEntity<APIResponse<ClassResponse>> updateClassById(
            @PathVariable UUID classId,
            @RequestBody @Valid ClassRequest request
    ) {
        return buildResponse("Class updated", classService.updateClassById(classId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{classId}")
    @Operation(
            summary = "Delete class",
            description = "Deletes a class by ID.",
            tags = {"Class"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Class not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteClassById(
            @PathVariable UUID classId
    ) {
        classService.deleteClassById(classId);
        return buildResponse("Class deleted", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/sub-subjects/{subSubjectId}")
    @Operation(
            summary = "Assign a sub-subject to a class",
            description = "Links an existing sub-subject to a class. Safe to call multiple times (idempotent).",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sub-subject assigned to class"),
                    @ApiResponse(responseCode = "404", description = "Class or Sub-subject not found"),
                    @ApiResponse(responseCode = "409", description = "Conflict (e.g., duplicate constraint)"),
            }
    )
    public ResponseEntity<APIResponse<Void>> assignSubSubjectToClass(
            @PathVariable UUID classId,
            @PathVariable UUID subSubjectId
    ) {
        classService.assignSubSubjectToClass(classId, subSubjectId);
        return buildResponse("Sub-subject assigned to class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/sub-subjects")
    @Operation(
            summary = "Assign multiple sub-subject to a class",
            description = "Links an existing sub-subject to a class. Safe to call multiple times (idempotent).",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sub-subject assigned to class"),
                    @ApiResponse(responseCode = "404", description = "Class or Sub-subject not found"),
                    @ApiResponse(responseCode = "409", description = "Conflict (e.g., duplicate constraint)"),
            }
    )
    public ResponseEntity<APIResponse<Void>> assignMultipleSubSubjectToClass(
            @PathVariable UUID classId,
            @RequestBody List<UUID> subSubjectIds
    ) {
        classService.assignMultipleSubSubjectToClass(classId, subSubjectIds);
        return buildResponse("Multiple sub-subject assigned to class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{classId}/sub-subjects/{subSubjectId}")
    @Operation(
            summary = "Unassign a sub-subject from a class",
            description = "Removes the link between the class and the sub-subject.",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sub-subject unassigned from class"),
                    @ApiResponse(responseCode = "404", description = "Class or Sub-subject not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> unassignSubSubjectFromClass(
            @PathVariable UUID classId,
            @PathVariable UUID subSubjectId
    ) {
        classService.unassignSubSubjectFromClass(classId, subSubjectId);
        return buildResponse("Sub-subject unassigned from class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/sub-subjects/{subSubjectId}/instructors/{instructorId}")
    @Operation(
            summary = "Assign instructor to a class–sub-subject",
            description = "Creates (or re-activates) an instructor assignment for the given Class/SubSubject. "
                          + "Idempotent: calling again with the same active assignment is OK.",
            tags = {"Instructor"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instructor assigned"),
                    @ApiResponse(responseCode = "404", description = "Class/SubSubject/Instructor not found"),
                    @ApiResponse(responseCode = "409", description = "Active assignment already exists (overlap)")
            }
    )
    public ResponseEntity<APIResponse<Void>> assignInstructorToClassSubSubject(
            @PathVariable UUID classId,
            @PathVariable UUID subSubjectId,
            @PathVariable UUID instructorId,
            @RequestBody @Valid AssignInstructorRequest request
    ) {
        classService.assignInstructorToClassSubSubject(
                classId,
                subSubjectId,
                instructorId,
                request.getStartDate()
        );
        return buildResponse("Instructor assigned to class sub-subject", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{classId}/sub-subjects/{subSubjectId}/instructors/{instructorId}")
    @Operation(
            summary = "Unassign instructor from a class–sub-subject",
            description = "Hard-removes the assignment link (use PATCH leave if you want to keep history via end_date).",
            tags = {"Instructor"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instructor unassigned"),
                    @ApiResponse(responseCode = "404", description = "Assignment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> unassignInstructorFromClassSubSubject(
            @PathVariable UUID classId,
            @PathVariable UUID subSubjectId,
            @PathVariable UUID instructorId
    ) {
        classService.unassignInstructorFromClassSubSubject(
                classId,
                subSubjectId,
                instructorId
        );
        return buildResponse("Instructor unassigned from class sub-subject", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{classId}/sub-subjects/{subSubjectId}/instructors/{instructorId}/leave")
    @Operation(
            summary = "Mark instructor as left from a class–sub-subject",
            description = "Sets end_date for the active assignment to the provided date (or today if using a default). "
                          + "Keeps historical record in class_sub_subject_instructors.",
            tags = {"Instructor"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instructor leave recorded"),
                    @ApiResponse(responseCode = "404", description = "Active assignment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> instructorLeaveClassSubSubject(
            @PathVariable UUID classId,
            @PathVariable UUID subSubjectId,
            @PathVariable UUID instructorId,
            @RequestBody @Valid LeaveInstructorRequest request
    ) {
        classService.leaveInstructorFromClassSubSubject(
                classId,
                subSubjectId,
                instructorId,
                request.getEndDate()
        );
        return buildResponse("Instructor leave recorded", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/students/{studentId}")
    @Operation(
            summary = "Enroll student to class",
            description = "Creates (or re-activates) an enrollment for the student in the class. Idempotent if already active.",
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student enrolled"),
                    @ApiResponse(responseCode = "404", description = "Class or Student not found"),
                    @ApiResponse(responseCode = "409", description = "Active enrollment already exists (overlap)")
            }
    )
    public ResponseEntity<APIResponse<Void>> enrollStudentToClass(
            @PathVariable UUID classId,
            @PathVariable UUID studentId,
            @RequestBody @Valid EnrollStudentRequest request
    ) {
        classService.enrollStudentToClass(
                classId,
                studentId,
                request.getStartDate()
        );
        return buildResponse("Student enrolled to class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/students")
    @Operation(
            summary = "Multiple enroll student to class",
            description = "Creates (or re-activates) multiple enrollment for the student in the class. Idempotent if already active.",
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student enrolled"),
                    @ApiResponse(responseCode = "404", description = "Class or Student not found"),
                    @ApiResponse(responseCode = "409", description = "Active enrollment already exists (overlap)")
            }
    )
    public ResponseEntity<APIResponse<Void>> multipleEnrollStudentToClass(
            @PathVariable UUID classId,
            @RequestBody List<UUID> studentIds
    ) {
        classService.multipleEnrollStudentToClass(
                classId,
                studentIds
        );
        return buildResponse("Student enrolled to class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{classId}/students/{studentId}")
    @Operation(
            summary = "Unenroll student from class (hard remove)",
            description = "Removes the enrollment row. Use PATCH /leave to keep history with an end_date instead.",
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student unenrolled"),
                    @ApiResponse(responseCode = "404", description = "Enrollment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> unenrollStudentFromClass(
            @PathVariable UUID classId,
            @PathVariable UUID studentId
    ) {
        classService.unenrollStudentFromClass(classId, studentId);
        return buildResponse("Student unenrolled from class", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{classId}/students/{studentId}/leave")
    @Operation(
            summary = "Mark student as left/completed",
            description = "Sets end_date on the active enrollment to preserve history (recommended).",
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student leave recorded"),
                    @ApiResponse(responseCode = "404", description = "Active enrollment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> studentLeaveClass(
            @PathVariable UUID classId,
            @PathVariable UUID studentId,
            @RequestBody LeaveOrCompleteStudentRequest request
    ) {
        classService.leaveStudentFromClass(classId, studentId, request.getEndDate());
        return buildResponse("Student leave recorded", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/move-student")
    @Operation(
            summary = "Move student to another class",
            description = """
                Moves a student from one class to another.
                This operation closes the current enrollment (sets end date)
                and creates a new active enrollment in the destination class.
                """,
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student moved successfully"),
                    @ApiResponse(responseCode = "404", description = "Class or Student not found"),
                    @ApiResponse(responseCode = "409", description = "Student already enrolled in the target class")
            }
    )
    public ResponseEntity<APIResponse<Void>> moveStudentToAnotherClass(
            @RequestBody @Valid MoveStudentRequest request
    ) {
        classService.moveStudentToAnotherClass(request);
        return buildResponse("Student moved successfully", null, HttpStatus.OK);
    }

    @GetMapping("/{classId}/sub-subjects")
    @Operation(
            summary = "List sub-subjects of a class",
            description = "Returns all sub-subjects assigned to the specified class, with pagination and sorting support.",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sub-subjects retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Class not found")
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<SubSubjectResponse>>>> getSubSubjectsOfClass(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "1") @Positive Integer page,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            @RequestParam(required = false, defaultValue = "NAME") ClassSubSubjectProperty property,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Sub-subjects retrieved successfully",
                classService.getSubSubjectsOfClass(classId, page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{classId}/students")
    @Operation(
            summary = "List students of a class",
            description = "Returns a paginated list of students currently enrolled in the specified class.",
            tags = {"Student"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Class not found")
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<StudentResponse>>>> getStudentsByClass(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse("Students retrieved successfully", classService.getStudentsByClass(classId, page, size, direction), HttpStatus.OK);
    }

    @GetMapping("/{classId}/instructors")
    @Operation(
            summary = "List instructors of a class",
            description = "Returns a paginated list of instructors currently assigned to any sub-subject under the specified class.",
            tags = {"Instructor"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instructors retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Class not found")
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<InstructorResponse>>>> getInstructorsByClass(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse("Instructors retrieved successfully", classService.getInstructorsByClass(classId, page, size, direction), HttpStatus.OK);
    }

    @GetMapping("/{classId}/summary")
    @Operation(
            summary = "Get class summary",
            description = "Returns aggregate counts for a class: sub-subjects, active students, and active instructors.",
            tags = {"Class"}
    )
    public ResponseEntity<APIResponse<ClassSummaryResponse>> getClassSummary(
            @PathVariable UUID classId
    ) {
        return buildResponse("Class summary retrieved", classService.getClassSummary(classId), HttpStatus.OK);
    }

    @GetMapping("/of-student/{studentId}")
    @Operation(
            summary = "List classes of a student",
            description = "Paginated classes the student is enrolled in. Use activeOnly to include history.",
            tags = {"Student"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<ClassResponse>>>> getClassesOfStudent(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "NAME") ClassProperty property,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse("Student classes retrieved", classService.getClassesOfStudent(studentId, page, size, property, direction), HttpStatus.OK);
    }

    @GetMapping("/of-instructor/{instructorId}")
    @Operation(
            summary = "List classes of an instructor",
            description = "Paginated classes where the instructor is assigned (defaults to active assignments).",
            tags = {"Instructor"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<ClassResponse>>>> getClassesOfInstructor(
            @PathVariable UUID instructorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "NAME") ClassProperty property,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse("Instructor classes retrieved", classService.getClassesOfInstructor(instructorId, page, size, property, direction), HttpStatus.OK);
    }

}
