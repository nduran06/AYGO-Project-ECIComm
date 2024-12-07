package com.aygo.eciComm.controller.analysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aygo.eciComm.exception.AnalysisException;
import com.aygo.eciComm.model.analysis.UserBehavior;
import com.aygo.eciComm.model.response.ErrorResponse;
import com.aygo.eciComm.service.analysis.BehaviorAnalysisService;

@RestController
@RequestMapping("/api/v1/analysis")
public class BehaviorAnalysisController {

	@Autowired
	private BehaviorAnalysisService analysisService;

	@GetMapping("/recommendations/{userId}")
	public ResponseEntity<List<String>> getRecommendations(@PathVariable String userId,
			@RequestBody UserBehavior behavior) {
		List<String> recommendations = analysisService.getProductRecommendations(userId, behavior);
		return ResponseEntity.ok(recommendations);
	}

	/*@GetMapping("/preferences/{userId}")
	public ResponseEntity<Map<String, Double>> getUserPreferences(@PathVariable String userId,
			@RequestBody UserBehavior behavior) {
		Map<String, Double> preferences = analysisService.predictUserPreferences(userId, behavior);
		return ResponseEntity.ok(preferences);
	}

	@GetMapping("/segment")
	public ResponseEntity<Map<String, Object>> analyzeSegment(@RequestBody UserBehavior behavior) {
		Map<String, Object> analysis = analysisService.analyzeUserSegment(behavior);
		return ResponseEntity.ok(analysis);
	}*/

	@ExceptionHandler(AnalysisException.class)
	public ResponseEntity<ErrorResponse> handleAnalysisError(AnalysisException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(),
				LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
