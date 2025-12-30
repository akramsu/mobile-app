package com.freshly.app.data.model

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // Index of correct answer
    val explanation: String,
    val category: QuizCategory
)

enum class QuizCategory {
    EXPIRATION,
    STORAGE,
    NUTRITION,
    FOOD_SAFETY
}

object QuizQuestions {
    val allQuestions = listOf(
        // EXPIRATION (15 questions)
        QuizQuestion(
            id = 1,
            question = "How long can eggs typically last in the refrigerator?",
            options = listOf("1-2 weeks", "3-5 weeks", "6-8 weeks", "2-3 months"),
            correctAnswer = 1,
            explanation = "Fresh eggs can last 3-5 weeks in the refrigerator when stored properly. The \"sell by\" date is usually 30 days from packing.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 5,
            question = "What does the \"best before\" date on packaged food mean?",
            options = listOf(
                "Food is unsafe after this date",
                "Food must be thrown away",
                "Peak quality, but still safe after",
                "Expiration date"
            ),
            correctAnswer = 2,
            explanation = "\"Best before\" indicates peak quality. Food is usually still safe to eat after this date if stored properly!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 11,
            question = "How long does opened yogurt last in the fridge?",
            options = listOf("3-5 days", "7-10 days", "2 weeks", "1 month"),
            correctAnswer = 1,
            explanation = "Opened yogurt stays fresh for 7-10 days when refrigerated. Always check for mold or off smells before consuming.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 12,
            question = "What's the shelf life of unopened canned goods?",
            options = listOf("6 months", "1 year", "2-5 years", "Forever"),
            correctAnswer = 2,
            explanation = "Unopened canned goods last 2-5 years when stored in a cool, dry place. Always check for dents or bulging before use.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 13,
            question = "How long can cooked rice be safely stored in the refrigerator?",
            options = listOf("1-2 days", "3-5 days", "1 week", "2 weeks"),
            correctAnswer = 1,
            explanation = "Cooked rice should be eaten within 3-5 days. Rice can harbor bacteria that multiply quickly if not stored properly.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 14,
            question = "How long do fresh berries typically last in the fridge?",
            options = listOf("1-2 days", "3-7 days", "2 weeks", "1 month"),
            correctAnswer = 1,
            explanation = "Fresh berries last 3-7 days when refrigerated. Don't wash them until ready to eat to prevent mold growth!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 15,
            question = "What's the maximum time cooked pasta can stay in the fridge?",
            options = listOf("1-2 days", "3-5 days", "1 week", "10 days"),
            correctAnswer = 1,
            explanation = "Cooked pasta stays fresh for 3-5 days when refrigerated in an airtight container. Add a bit of oil to prevent sticking.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 16,
            question = "How long does hard cheese last after opening?",
            options = listOf("1 week", "2-3 weeks", "3-4 weeks", "2 months"),
            correctAnswer = 2,
            explanation = "Hard cheeses like cheddar or parmesan last 3-4 weeks after opening. Wrap tightly to prevent drying out.",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 36,
            question = "How long can fresh fish stay in the fridge?",
            options = listOf("1 day", "1-2 days", "3-4 days", "1 week"),
            correctAnswer = 1,
            explanation = "Fresh fish should be cooked within 1-2 days for best quality and safety. Keep it on ice in the fridge!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 37,
            question = "How long does butter last in the refrigerator?",
            options = listOf("1 week", "2 weeks", "1 month", "3 months"),
            correctAnswer = 2,
            explanation = "Butter can last about 1 month in the fridge when properly wrapped. It can also be frozen for longer storage!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 38,
            question = "Can you eat food after the expiration date?",
            options = listOf(
                "Never safe",
                "Sometimes, use your senses",
                "Always safe",
                "Only if refrigerated"
            ),
            correctAnswer = 1,
            explanation = "Expiration dates are guidelines. Many foods are safe past this date if stored properly - always check smell, appearance, and texture!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 39,
            question = "How long can you keep leftover pizza in the fridge?",
            options = listOf("1 day", "2-3 days", "3-4 days", "1 week"),
            correctAnswer = 2,
            explanation = "Leftover pizza stays safe for 3-4 days when refrigerated. Reheat thoroughly before eating!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 40,
            question = "How long does opened salsa last in the fridge?",
            options = listOf("3-5 days", "1-2 weeks", "3-4 weeks", "2 months"),
            correctAnswer = 1,
            explanation = "Opened salsa lasts 1-2 weeks when refrigerated. Always use a clean spoon to avoid contamination!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 41,
            question = "What does 'use by' date mean?",
            options = listOf(
                "Best quality date",
                "Last safe date to consume",
                "Manufacturing date",
                "Packaging date"
            ),
            correctAnswer = 1,
            explanation = "'Use by' is the last date recommended for consumption for safety reasons. Don't eat food past this date!",
            category = QuizCategory.EXPIRATION
        ),
        QuizQuestion(
            id = 42,
            question = "How long can fresh mushrooms stay in the fridge?",
            options = listOf("2-3 days", "4-7 days", "2 weeks", "1 month"),
            correctAnswer = 1,
            explanation = "Fresh mushrooms last 4-7 days when stored in a paper bag in the fridge. Don't wash them until ready to use!",
            category = QuizCategory.EXPIRATION
        ),
        
        // STORAGE (10 questions)
        QuizQuestion(
            id = 2,
            question = "What's the best way to store fresh herbs to keep them longer?",
            options = listOf(
                "In a paper bag in the fridge",
                "In water like flowers",
                "Wrapped in plastic wrap",
                "Left on the counter"
            ),
            correctAnswer = 1,
            explanation = "Storing fresh herbs in water (like a bouquet) and covering them loosely with plastic can extend their life up to 2 weeks!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 3,
            question = "Which food should NOT be stored in the refrigerator?",
            options = listOf("Carrots", "Tomatoes", "Lettuce", "Grapes"),
            correctAnswer = 1,
            explanation = "Tomatoes lose flavor and become mealy when refrigerated. Store them at room temperature for best taste!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 6,
            question = "Where should you store bread to keep it fresh longest?",
            options = listOf("Refrigerator", "Freezer", "Room temperature", "In direct sunlight"),
            correctAnswer = 2,
            explanation = "Bread stays fresh longest at room temperature (3-4 days). Refrigeration makes it stale faster, but freezing is great for long-term storage!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 8,
            question = "Which fruit should NOT be stored with other fruits?",
            options = listOf("Apples", "Grapes", "Blueberries", "Strawberries"),
            correctAnswer = 0,
            explanation = "Apples release ethylene gas which makes other fruits ripen (and spoil) faster. Store them separately!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 17,
            question = "Should you wash vegetables before storing them?",
            options = listOf(
                "Yes, always wash first",
                "No, wash right before use",
                "Only leafy greens",
                "Only root vegetables"
            ),
            correctAnswer = 1,
            explanation = "Wash vegetables right before use! Excess moisture during storage promotes mold and bacterial growth.",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 18,
            question = "Where should you store onions for maximum freshness?",
            options = listOf(
                "In the refrigerator",
                "In a cool, dry, dark place",
                "Next to potatoes",
                "In direct sunlight"
            ),
            correctAnswer = 1,
            explanation = "Onions last longest in a cool, dry, dark place with good air circulation. Keep them away from potatoes!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 19,
            question = "What's the best way to store leftover soup?",
            options = listOf(
                "Leave it in the pot",
                "Store in shallow containers",
                "Keep it on the stovetop",
                "Wait until it's room temperature"
            ),
            correctAnswer = 1,
            explanation = "Store soup in shallow containers to cool it quickly and prevent bacterial growth. Refrigerate within 2 hours!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 20,
            question = "Should you store bananas in the refrigerator?",
            options = listOf(
                "Never",
                "Only when green",
                "Only when ripe",
                "Always"
            ),
            correctAnswer = 2,
            explanation = "Refrigerate bananas once they're ripe to slow down further ripening. The peel may darken, but the fruit inside stays good!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 21,
            question = "What's the best container for storing lettuce?",
            options = listOf(
                "Plastic bag with no holes",
                "Paper towel in a container",
                "Sealed glass jar",
                "Open bowl"
            ),
            correctAnswer = 1,
            explanation = "Wrap lettuce in paper towels and store in a container. The towels absorb excess moisture, keeping it crisp for up to 2 weeks!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 22,
            question = "Should you remove the tops of carrots before storing?",
            options = listOf(
                "No, leave them on",
                "Yes, remove them",
                "Only if organic",
                "Only if wilted"
            ),
            correctAnswer = 1,
            explanation = "Remove carrot tops! They draw moisture from the carrots, making them soft and wilted. Store tops separately if you plan to use them.",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 43,
            question = "Should you store potatoes in the refrigerator?",
            options = listOf(
                "Yes, always",
                "No, store in cool dark place",
                "Only sweet potatoes",
                "Only if cooked"
            ),
            correctAnswer = 1,
            explanation = "Don't refrigerate potatoes! Cold temperatures turn their starch to sugar. Store in a cool, dark, dry place instead.",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 44,
            question = "How should you store garlic?",
            options = listOf(
                "In the fridge",
                "In water",
                "In a cool, dry place",
                "In the freezer"
            ),
            correctAnswer = 2,
            explanation = "Store garlic in a cool, dry place with good air circulation. Don't refrigerate whole garlic - it can sprout!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 45,
            question = "Where should you store avocados to ripen?",
            options = listOf(
                "In the refrigerator",
                "At room temperature",
                "In the freezer",
                "In water"
            ),
            correctAnswer = 1,
            explanation = "Ripen avocados at room temperature. Once ripe, refrigerate to slow down further ripening!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 46,
            question = "Should you store coffee in the freezer?",
            options = listOf(
                "Yes, always",
                "No, it damages flavor",
                "Only ground coffee",
                "Only instant coffee"
            ),
            correctAnswer = 1,
            explanation = "Don't freeze coffee! It absorbs odors and moisture affects flavor. Store in an airtight container in a cool, dark place.",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 47,
            question = "What's the best way to store cucumbers?",
            options = listOf(
                "At room temperature",
                "In the crisper drawer",
                "In the freezer",
                "In water"
            ),
            correctAnswer = 1,
            explanation = "Store cucumbers in the crisper drawer of your fridge, but not with ethylene-producing fruits like tomatoes!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 48,
            question = "Should honey be refrigerated?",
            options = listOf(
                "Yes, always",
                "No, store at room temperature",
                "Only after opening",
                "Only in summer"
            ),
            correctAnswer = 1,
            explanation = "Never refrigerate honey! It crystallizes when cold. Store at room temperature - honey never spoils!",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 49,
            question = "How should you store fresh ginger?",
            options = listOf(
                "At room temperature",
                "In the fridge unpeeled",
                "In the freezer peeled",
                "In water"
            ),
            correctAnswer = 1,
            explanation = "Store fresh ginger unpeeled in the fridge. It can last for weeks! You can also freeze it for longer storage.",
            category = QuizCategory.STORAGE
        ),
        QuizQuestion(
            id = 50,
            question = "Should you wash eggs before storing?",
            options = listOf(
                "Yes, always wash them",
                "No, don't wash before storing",
                "Only if dirty",
                "Only organic eggs"
            ),
            correctAnswer = 1,
            explanation = "Don't wash eggs before storing! The natural coating protects them. Wash right before use if needed.",
            category = QuizCategory.STORAGE
        ),
        
        // FOOD_SAFETY (10 questions)
        QuizQuestion(
            id = 4,
            question = "How long can cooked chicken safely stay in the fridge?",
            options = listOf("1-2 days", "3-4 days", "5-6 days", "1 week"),
            correctAnswer = 1,
            explanation = "Cooked chicken should be eaten within 3-4 days when stored in the refrigerator at 40°F or below.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 7,
            question = "How can you tell if milk has gone bad?",
            options = listOf(
                "Check the date only",
                "Smell, taste, and texture test",
                "Color change",
                "It's always bad after the date"
            ),
            correctAnswer = 1,
            explanation = "Use your senses! If milk smells sour, tastes off, or has lumps, it's bad. The date is just a guide - milk often lasts several days past it.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 9,
            question = "How long can you safely freeze meat?",
            options = listOf("1 month", "3-4 months", "6-12 months", "Forever"),
            correctAnswer = 2,
            explanation = "Frozen meat stays safe indefinitely, but for best quality, use within 6-12 months depending on the type of meat.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 10,
            question = "What's the ideal refrigerator temperature?",
            options = listOf("32°F (0°C)", "37°F (3°C)", "45°F (7°C)", "50°F (10°C)"),
            correctAnswer = 1,
            explanation = "The ideal refrigerator temperature is 37°F (3°C) or below to slow bacterial growth and keep food fresh longer.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 23,
            question = "How long can food safely sit out at room temperature?",
            options = listOf("30 minutes", "1 hour", "2 hours", "4 hours"),
            correctAnswer = 2,
            explanation = "Food should not sit out for more than 2 hours (1 hour if temperature is above 90°F). This is the \"danger zone\" for bacterial growth!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 24,
            question = "What's the safe internal temperature for cooking chicken?",
            options = listOf("145°F (63°C)", "155°F (68°C)", "165°F (74°C)", "175°F (79°C)"),
            correctAnswer = 2,
            explanation = "Chicken must reach an internal temperature of 165°F (74°C) to kill harmful bacteria like salmonella.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 25,
            question = "Is it safe to eat food with a little bit of mold removed?",
            options = listOf(
                "Yes, always",
                "Only hard foods like cheese",
                "Only fruits",
                "Never"
            ),
            correctAnswer = 1,
            explanation = "Hard foods like hard cheese or firm vegetables can be salvaged by cutting off 1 inch around the mold. Soft foods should be discarded entirely.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 26,
            question = "Should you wash raw chicken before cooking?",
            options = listOf(
                "Yes, always",
                "No, it spreads bacteria",
                "Only if it looks dirty",
                "Only organic chicken"
            ),
            correctAnswer = 1,
            explanation = "Never wash raw chicken! It spreads harmful bacteria around your kitchen. Cooking to 165°F kills all bacteria.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 27,
            question = "How should you thaw frozen meat safely?",
            options = listOf(
                "On the counter",
                "In the refrigerator",
                "In hot water",
                "In direct sunlight"
            ),
            correctAnswer = 1,
            explanation = "Thaw meat in the refrigerator to keep it at a safe temperature. Counter thawing allows bacteria to multiply in the outer layers!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 28,
            question = "Can you refreeze meat that has been thawed?",
            options = listOf(
                "Never",
                "Yes, if thawed in the fridge",
                "Only if cooked first",
                "Only within 1 hour"
            ),
            correctAnswer = 1,
            explanation = "You can safely refreeze meat if it was thawed in the refrigerator and hasn't been left at room temperature for more than 2 hours.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 51,
            question = "What's the safe temperature for hot food storage?",
            options = listOf("100°F (38°C)", "120°F (49°C)", "140°F (60°C)", "180°F (82°C)"),
            correctAnswer = 2,
            explanation = "Hot food should be kept at 140°F (60°C) or above to prevent bacterial growth. This is above the danger zone!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 52,
            question = "Can you tell if food is safe by smelling it?",
            options = listOf(
                "Yes, always",
                "No, not always",
                "Only for dairy",
                "Only for meat"
            ),
            correctAnswer = 1,
            explanation = "Smell helps, but some harmful bacteria don't have odors. Always follow storage time guidelines for safety!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 53,
            question = "Is it safe to eat raw cookie dough?",
            options = listOf(
                "Yes, perfectly safe",
                "No, risk from raw eggs and flour",
                "Only if organic",
                "Only if refrigerated"
            ),
            correctAnswer = 1,
            explanation = "Raw cookie dough isn't safe due to raw eggs (salmonella risk) and raw flour (E. coli risk). Bake it first!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 54,
            question = "How long can leftovers stay at room temperature?",
            options = listOf("30 minutes", "1 hour", "2 hours", "4 hours"),
            correctAnswer = 2,
            explanation = "Leftovers should be refrigerated within 2 hours (1 hour if over 90°F). The danger zone is 40-140°F!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 55,
            question = "Should you rinse canned beans before using?",
            options = listOf(
                "No, never",
                "Yes, reduces sodium",
                "Only black beans",
                "Only organic beans"
            ),
            correctAnswer = 1,
            explanation = "Rinsing canned beans reduces sodium by up to 40%! It also removes the thick liquid and improves texture.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 56,
            question = "What color should cooked pork be?",
            options = listOf("Pink in center", "Light pink", "White/tan throughout", "Brown"),
            correctAnswer = 2,
            explanation = "Cooked pork should be white or tan throughout with no pink. Internal temperature should reach 145°F (63°C)!",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 57,
            question = "Can you eat moldy bread if you remove the moldy part?",
            options = listOf(
                "Yes, safe",
                "No, mold spreads invisibly",
                "Only hard bread",
                "Only toasted"
            ),
            correctAnswer = 1,
            explanation = "Don't eat moldy bread! Mold has invisible roots that spread through soft foods. Throw it away entirely.",
            category = QuizCategory.FOOD_SAFETY
        ),
        QuizQuestion(
            id = 58,
            question = "How often should you clean your refrigerator?",
            options = listOf("Once a year", "Every 6 months", "Monthly", "Weekly"),
            correctAnswer = 2,
            explanation = "Clean your fridge monthly to prevent bacteria growth and food contamination. Wipe spills immediately!",
            category = QuizCategory.FOOD_SAFETY
        ),
        
        // NUTRITION (7 questions)
        QuizQuestion(
            id = 29,
            question = "Which vegetable loses the most nutrients when overcooked?",
            options = listOf("Carrots", "Broccoli", "Potatoes", "Onions"),
            correctAnswer = 1,
            explanation = "Broccoli loses significant vitamin C and other nutrients when overcooked. Steam lightly for 3-5 minutes to preserve nutrients!",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 30,
            question = "Does freezing vegetables reduce their nutritional value?",
            options = listOf(
                "Yes, significantly",
                "No, it preserves nutrients",
                "Only vitamin C is lost",
                "Only if frozen for over 6 months"
            ),
            correctAnswer = 1,
            explanation = "Freezing actually locks in nutrients! Frozen vegetables can be more nutritious than \"fresh\" ones that have been sitting for days.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 31,
            question = "Which part of the broccoli has the most nutrients?",
            options = listOf("Florets only", "Stem only", "Both equally", "Leaves"),
            correctAnswer = 2,
            explanation = "Both the florets and stems are packed with nutrients! Don't waste the stem - peel and slice it for cooking.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 32,
            question = "Do brown eggs have more nutrients than white eggs?",
            options = listOf(
                "Yes, much more",
                "No, they're the same",
                "Only slightly more protein",
                "Yes, more calcium"
            ),
            correctAnswer = 1,
            explanation = "Brown and white eggs have identical nutrition! The color depends on the breed of chicken, not nutritional content.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 33,
            question = "Does cooking tomatoes increase their nutritional value?",
            options = listOf(
                "No, it destroys nutrients",
                "Yes, it increases lycopene",
                "Only if grilled",
                "Only if canned"
            ),
            correctAnswer = 1,
            explanation = "Cooking tomatoes increases lycopene, a powerful antioxidant! Cooked tomatoes can be more nutritious than raw ones.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 34,
            question = "Which cooking method preserves the most nutrients?",
            options = listOf("Boiling", "Steaming", "Frying", "Microwaving"),
            correctAnswer = 1,
            explanation = "Steaming preserves the most nutrients because food doesn't sit in water. Microwaving is also great for nutrient retention!",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 35,
            question = "Does peeling fruits and vegetables reduce nutrients?",
            options = listOf(
                "No difference",
                "Yes, significantly",
                "Only for root vegetables",
                "Only for organic produce"
            ),
            correctAnswer = 1,
            explanation = "Many nutrients are concentrated in or just under the skin! Wash well and leave the peel on when possible.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 59,
            question = "Does microwaving food destroy nutrients?",
            options = listOf(
                "Yes, completely",
                "No, it preserves them well",
                "Only vitamins",
                "Only minerals"
            ),
            correctAnswer = 1,
            explanation = "Microwaving is great for nutrients! Short cooking time and minimal water preserve vitamins better than boiling.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 60,
            question = "Are fresh vegetables always more nutritious than frozen?",
            options = listOf(
                "Yes, always",
                "No, frozen can be better",
                "Only organic fresh",
                "Only in summer"
            ),
            correctAnswer = 1,
            explanation = "Frozen vegetables are flash-frozen at peak ripeness, locking in nutrients. Sometimes they're more nutritious than 'fresh' produce that's been sitting for days!",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 61,
            question = "Which has more vitamin C - red or green peppers?",
            options = listOf("Green", "Red", "Same amount", "Yellow"),
            correctAnswer = 1,
            explanation = "Red peppers have nearly 3 times more vitamin C than green! They're also sweeter because they're more ripe.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 62,
            question = "Does adding salt to cooking water reduce nutrients?",
            options = listOf(
                "Yes, significantly",
                "No, doesn't affect nutrients",
                "Only for pasta",
                "Only for vegetables"
            ),
            correctAnswer = 1,
            explanation = "Salt doesn't reduce nutrients in vegetables. It can actually help retain color and flavor during cooking!",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 63,
            question = "Are baby carrots less nutritious than regular carrots?",
            options = listOf(
                "Yes, much less",
                "No, same nutrition",
                "Yes, slightly less",
                "Only if organic"
            ),
            correctAnswer = 1,
            explanation = "Baby carrots are just regular carrots cut and peeled. They have the same nutrition! Buy whichever you prefer.",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 64,
            question = "Does cooking spinach increase iron absorption?",
            options = listOf(
                "No, it decreases it",
                "Yes, makes it easier to absorb",
                "No effect",
                "Only if steamed"
            ),
            correctAnswer = 1,
            explanation = "Cooking spinach breaks down oxalic acid, making iron more available for absorption. Plus you can eat more cooked spinach!",
            category = QuizCategory.NUTRITION
        ),
        QuizQuestion(
            id = 65,
            question = "Which apple has the most nutrients?",
            options = listOf(
                "Red apples",
                "Green apples",
                "All similar",
                "Yellow apples"
            ),
            correctAnswer = 2,
            explanation = "All apple varieties have similar nutrition! Choose based on taste preference - tart, sweet, or somewhere in between.",
            category = QuizCategory.NUTRITION
        )
    )
    
    fun getRandomQuestions(count: Int = 5): List<QuizQuestion> {
        return allQuestions.shuffled().take(count)
    }
}
