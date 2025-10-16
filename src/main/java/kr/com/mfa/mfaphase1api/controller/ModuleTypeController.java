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
import kr.com.mfa.mfaphase1api.model.dto.request.ModuleTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.ModuleTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.ModuleTypeProperty;
import kr.com.mfa.mfaphase1api.service.ModuleTypeService;
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
@RequestMapping("/api/v1/modules-types")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class ModuleTypeController {

    private final ModuleTypeService moduleTypeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create module type",
            description = "Creates a new module type. Module Type name must be unique (case-insensitive).",
            tags = {"ModuleType"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = ModuleTypeResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Type already exists")
            }
    )
    public ResponseEntity<APIResponse<ModuleTypeResponse>> createModuleType(
            @RequestBody @Valid ModuleTypeRequest request
    ) {
        return buildResponse("Module Type Created", moduleTypeService.createModuleType(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List module types",
            description = "Returns a paginated list of module types with sorting.",
            tags = {"ModuleType"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<ModuleTypeResponse>>>> getAllModuleTypes(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "TYPE")
            @RequestParam(required = false, defaultValue = "TYPE") ModuleTypeProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Module Types retrieved",
                moduleTypeService.getAllModuleTypes(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{moduleTypeId}")
    @Operation(
            summary = "Get module type",
            description = "Returns a module type by its ID.",
            tags = {"ModuleType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = ModuleTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "ModuleType not found")
            }
    )
    public ResponseEntity<APIResponse<ModuleTypeResponse>> getModuleTypeById(
            @PathVariable UUID moduleTypeId
    ) {
        return buildResponse("Module Type retrieved", moduleTypeService.getModuleTypeById(moduleTypeId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{moduleTypeId}")
    @Operation(
            summary = "Update module type",
            description = "Updates a module type by ID. Module type must remain unique.",
            tags = {"ModuleType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = ModuleTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "ModuleType not found"),
                    @ApiResponse(responseCode = "409", description = "Type already exists")
            }
    )
    public ResponseEntity<APIResponse<ModuleTypeResponse>> updateModuleTypeById(
            @PathVariable UUID moduleTypeId,
            @RequestBody @Valid ModuleTypeRequest request
    ) {
        return buildResponse("Module Type updated", moduleTypeService.updateModuleTypeById(moduleTypeId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{moduleTypeId}")
    @Operation(
            summary = "Delete module type",
            description = "Deletes a module type by ID.",
            tags = {"ModuleType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "ModuleType not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteModuleTypeById(
            @PathVariable UUID moduleTypeId
    ) {
        moduleTypeService.deleteModuleTypeById(moduleTypeId);
        return buildResponse("Module Type deleted", null, HttpStatus.OK);
    }

}
