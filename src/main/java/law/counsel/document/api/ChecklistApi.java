/*───────────────────────────────────────────────────────────
  src/main/java/law/counsel/document/api/ChecklistApi.java
───────────────────────────────────────────────────────────*/
package law.counsel.document.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import law.counsel.document.domain.ContractType;
import law.counsel.document.dto.ChecklistBatchSaveDto;
import law.counsel.document.dto.ChecklistItemDto;
import law.counsel.global.config.swagger.SwaggerApiFailedResponse;
import law.counsel.global.config.swagger.SwaggerApiResponses;
import law.counsel.global.config.swagger.SwaggerApiSuccessResponse;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.response.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  문서별 체크리스트 조회·저장 Swagger 인터페이스
 *
 *  구현체(Controller)는 ChecklistController 가 implements ChecklistApi 만 붙이면 됩니다.
 */
@Tag(name = "체크리스트 API", description = "문서별 체크리스트 조회·저장")
@RequestMapping("/api/documents/{documentId}/checklist")
public interface ChecklistApi {

    /*───────────────────────────
      1) 체크리스트 조회 (GET)
    ───────────────────────────*/
    @Operation(
            summary     = "체크리스트 조회",
            description = """
                      문서 ID와 계약 유형을 전달하면<br>
                      ➜ 해당 템플릿 문항을 반환합니다.
                      """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    description = "조회 성공",
                    response    = ChecklistItemDto.class
            ),
            errors = {
                    @SwaggerApiFailedResponse(
                            value       = ExceptionType.TEMPLATE_NOT_FOUND,
                            description = "해당 계약 유형의 템플릿이 없을 때"
                    )
            }
    )
    @GetMapping
    ResponseEntity<ResponseBody<List<ChecklistItemDto>>> getChecklist(
            @Parameter(
                    in          = ParameterIn.PATH,
                    description = "문서 PK",
                    required    = true)
            @PathVariable Long documentId,

            @Parameter(
                    in          = ParameterIn.QUERY,
                    schema      = @Schema(implementation = ContractType.class),
                    description = "문서 계약 유형 (NDA · EMPLOY · SUB …)",
                    required    = true)
            @RequestParam ContractType contractType);

    /*───────────────────────────
      2) 체크/언체크 일괄 저장 (POST)
    ───────────────────────────*/
    @Operation(
            summary     = "체크/언체크 일괄 저장",
            description = """
                      여러 문항의 체크 상태를 한 번에 저장합니다.
                      Body 형식:<br>
                      <pre>{
    "items":[
      {"itemId":1001,"isChecked":true},
      {"itemId":1002,"isChecked":false}
    ]
}</pre>
                      """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    description = "저장 성공"
            )
    )
    @PostMapping("/bulk")
    ResponseEntity<ResponseBody<Void>> saveBulk(
            @Parameter(
                    in          = ParameterIn.PATH,
                    description = "문서 PK",
                    required    = true)
            @PathVariable Long documentId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문항 ID와 체크 여부 배열",
                    required    = true)
            @Valid @RequestBody ChecklistBatchSaveDto dto);
}
