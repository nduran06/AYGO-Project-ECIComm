package com.aygo.eciComm.tools.data;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.opencsv.CSVWriter;

public class EciCommDataGenerator {
	private static final Random random = new Random();

	// Product categories and subcategories
	private static final Map<String, List<String>> PRODUCT_CATEGORIES = Map.of("ELECTRONICS",
			Arrays.asList("Laptops", "Smartphones", "Tablets", "Accessories"), "CLOTHING",
			Arrays.asList("Men", "Women", "Kids", "Sports"), "BOOKS",
			Arrays.asList("Fiction", "Non-Fiction", "Technical", "Children"), "HOME",
			Arrays.asList("Kitchen", "Furniture", "Decor", "Garden"));

	// Price ranges by category
	private static final Map<String, Map<String, double[]>> PRICE_RANGES = Map.of("ELECTRONICS",
			Map.of("Laptops", new double[] { 500.0, 3000.0 }, "Smartphones", new double[] { 200.0, 1500.0 }, "Tablets",
					new double[] { 100.0, 1000.0 }, "Accessories", new double[] { 10.0, 200.0 }),
			"CLOTHING", Map.of("Men", new double[] { 20.0, 200.0 }, "Women", new double[] { 20.0, 200.0 }, "Kids",
					new double[] { 10.0, 100.0 }, "Sports", new double[] { 30.0, 300.0 }));

	public static void main(String[] args) {
		// Generate files for training
		generateUserProfiles("user_profiles.csv", 1000);
		generateProducts("products.csv", 500);
		generateUserBehavior("user_behavior.csv", 10000);
		generateOrders("orders.csv", "order_items.csv", 5000);
		generateProductRecommendations("product_recommendations.csv", 1000);
	}

	private static void generateUserProfiles(String filename, int count) {
		try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
			writer.writeNext(new String[] { "user_id", "age_group", "gender", "location", "preferred_categories",
					"avg_order_value", "total_orders", "member_since", "last_purchase_date" });

			for (int i = 0; i < count; i++) {
				String userId = String.format("USR_%06d", i);
				String ageGroup = getRandomAgeGroup();
				String gender = random.nextBoolean() ? "M" : "F";
				String location = getRandomLocation();
				String preferredCategories = String.join("|", getRandomCategories(3));

				// Ensure positive values for calculations
				double avgOrderValue = 50 + random.nextDouble() * 450;
				int totalOrders = 1 + random.nextInt(50); // At least 1 order

				// Calculate dates ensuring proper ranges
				LocalDate now = LocalDate.now();
				LocalDate memberSince = now.minusDays(1 + random.nextInt(365 * 2)); // At least 1 day ago

				// Calculate last purchase date between membership date and now
				long daysBetween = ChronoUnit.DAYS.between(memberSince, now);
				LocalDate lastPurchase = memberSince.plusDays(1 + random.nextInt(Math.max(1, (int) daysBetween)));

				writer.writeNext(new String[] { userId, ageGroup, gender, location, preferredCategories,
						String.format("%.2f", avgOrderValue), String.valueOf(totalOrders), memberSince.toString(),
						lastPurchase.toString() });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateProducts(String filename, int count) {
		try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
			writer.writeNext(new String[] { "product_id", "name", "category", "subcategory", "price", "avg_rating",
					"total_reviews", "stock_level", "created_date" });

			for (int i = 0; i < count; i++) {
				String productId = String.format("PROD_%06d", i);
				String category = getRandomCategory();
				String subcategory = getRandomSubcategory(category);
				double price = generatePrice(category, subcategory);
				double avgRating = 3.0 + random.nextDouble() * 2.0;
				int totalReviews = random.nextInt(1000);
				int stockLevel = random.nextInt(1000);
				LocalDate createdDate = LocalDate.now().minusDays(random.nextInt(365));

				writer.writeNext(new String[] { productId, generateProductName(category, subcategory), category,
						subcategory, String.format("%.2f", price), String.format("%.1f", avgRating),
						String.valueOf(totalReviews), String.valueOf(stockLevel), createdDate.toString() });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateUserBehavior(String filename, int count) {
		try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
			writer.writeNext(new String[] { "user_id", "product_id", "timestamp", "action", "session_id", "time_spent",
					"add_to_cart", "purchase", "page_views" });

			for (int i = 0; i < count; i++) {
				String userId = String.format("USR_%06d", random.nextInt(1000));
				String productId = String.format("PROD_%06d", random.nextInt(500));
				LocalDateTime timestamp = LocalDateTime.now().minusDays(random.nextInt(90));
				String sessionId = UUID.randomUUID().toString();
				String action = getRandomAction();
				int timeSpent = random.nextInt(300);
				boolean addToCart = random.nextDouble() < 0.3;
				boolean purchase = addToCart && random.nextDouble() < 0.4;
				int pageViews = random.nextInt(10);

				writer.writeNext(new String[] { userId, productId, timestamp.toString(), action, sessionId,
						String.valueOf(timeSpent), String.valueOf(addToCart), String.valueOf(purchase),
						String.valueOf(pageViews) });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateOrders(String ordersFile, String orderItemsFile, int count) {
		try (CSVWriter orderWriter = new CSVWriter(new FileWriter(ordersFile));
				CSVWriter itemWriter = new CSVWriter(new FileWriter(orderItemsFile))) {

			// Write order headers
			orderWriter.writeNext(new String[] { "order_id", "user_id", "order_date", "total_amount", "status",
					"payment_method", "shipping_address" });

			// Write order items headers
			itemWriter.writeNext(
					new String[] { "order_item_id", "order_id", "product_id", "quantity", "unit_price", "subtotal" });

			for (int i = 0; i < count; i++) {
				String orderId = String.format("ORD_%06d", i);
				String userId = String.format("USR_%06d", random.nextInt(1000));
				LocalDateTime orderDate = LocalDateTime.now().minusDays(random.nextInt(90));
				double totalAmount = 0.0;
				String status = getRandomOrderStatus();
				String paymentMethod = getRandomPaymentMethod();
				String shippingAddress = getRandomLocation();

				// Generate between 1 and 5 items per order
				int itemCount = 1 + random.nextInt(4);
				for (int j = 0; j < itemCount; j++) {
					String orderItemId = String.format("ORDITEM_%s_%d", orderId, j);
					String productId = String.format("PROD_%06d", random.nextInt(500));
					int quantity = 1 + random.nextInt(3);
					double unitPrice = 10.0 + random.nextDouble() * 990.0;
					double subtotal = quantity * unitPrice;
					totalAmount += subtotal;

					itemWriter.writeNext(new String[] { orderItemId, orderId, productId, String.valueOf(quantity),
							String.format("%.2f", unitPrice), String.format("%.2f", subtotal) });
				}

				orderWriter.writeNext(new String[] { orderId, userId, orderDate.toString(),
						String.format("%.2f", totalAmount), status, paymentMethod, shippingAddress });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateProductRecommendations(String filename, int count) {
		try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
			writer.writeNext(new String[] { "user_id", "recommended_product_id", "recommendation_score",
					"recommendation_type", "generated_date" });

			for (int i = 0; i < count; i++) {
				String userId = String.format("USR_%06d", random.nextInt(1000));

				// Generate 5 recommendations per user
				for (int j = 0; j < 5; j++) {
					String productId = String.format("PROD_%06d", random.nextInt(500));
					double score = 0.5 + random.nextDouble() * 0.5;
					String recType = getRandomRecommendationType();
					LocalDateTime generatedDate = LocalDateTime.now().minusHours(random.nextInt(24));

					writer.writeNext(new String[] { userId, productId, String.format("%.3f", score), recType,
							generatedDate.toString() });
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Helper methods
	private static String getRandomAgeGroup() {
		String[] groups = { "18-24", "25-34", "35-44", "45-54", "55-64", "65+" };
		return groups[random.nextInt(groups.length)];
	}

	private static String getRandomLocation() {
		String[] locations = { "New York", "Los Angeles", "Chicago", "Houston", "Miami" };
		return locations[random.nextInt(locations.length)];
	}

	private static String getRandomCategory() {
		List<String> categories = new ArrayList<>(PRODUCT_CATEGORIES.keySet());
		return categories.get(random.nextInt(categories.size()));
	}

	private static String getRandomSubcategory(String category) {
		List<String> subcategories = PRODUCT_CATEGORIES.get(category);
		return subcategories.get(random.nextInt(subcategories.size()));
	}

	private static List<String> getRandomCategories(int count) {
		List<String> allCategories = new ArrayList<>(PRODUCT_CATEGORIES.keySet());
		Collections.shuffle(allCategories);
		return allCategories.subList(0, Math.min(count, allCategories.size()));
	}

	private static String getRandomAction() {
		String[] actions = { "VIEW", "ADD_TO_CART", "PURCHASE", "WISHLIST", "REVIEW" };
		return actions[random.nextInt(actions.length)];
	}

	private static String getRandomOrderStatus() {
		String[] statuses = { "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED" };
		return statuses[random.nextInt(statuses.length)];
	}

	private static String getRandomPaymentMethod() {
		String[] methods = { "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER" };
		return methods[random.nextInt(methods.length)];
	}

	private static String getRandomRecommendationType() {
		String[] types = { "COLLABORATIVE", "CONTENT_BASED", "POPULARITY", "TRENDING" };
		return types[random.nextInt(types.length)];
	}

	private static double generatePrice(String category, String subcategory) {
		double[] range = PRICE_RANGES.getOrDefault(category, Map.of(subcategory, new double[] { 10.0, 100.0 }))
				.getOrDefault(subcategory, new double[] { 10.0, 100.0 });
		return range[0] + random.nextDouble() * (range[1] - range[0]);
	}

	private static String generateProductName(String category, String subcategory) {
		// You can expand this with more realistic product names
		return String.format("%s %s %s", subcategory, getRandomBrand(category), getRandomModel());
	}

	private static String getRandomBrand(String category) {
		Map<String, String[]> brandsByCategory = Map.of("ELECTRONICS",
				new String[] { "Apple", "Samsung", "Dell", "HP", "Lenovo" }, "CLOTHING",
				new String[] { "Nike", "Adidas", "Puma", "Under Armour", "Levi's" });
		String[] brands = brandsByCategory.getOrDefault(category,
				new String[] { "Generic", "Basic", "Premium", "Value" });
		return brands[random.nextInt(brands.length)];
	}

	private static String getRandomModel() {
		return String.format("%d%s", 2020 + random.nextInt(5), random.nextInt(100));
	}
}