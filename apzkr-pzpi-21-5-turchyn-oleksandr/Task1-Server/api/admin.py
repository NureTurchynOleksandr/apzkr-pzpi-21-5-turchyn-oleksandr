from django.contrib import admin
from . import models


api_models = [
    models.AutomaticMachineDish,
    models.AutomaticMachineType,
    models.CateringEstablishment,
    models.CateringEstablishmentAutomaticMachine,
    models.Dish,
    models.DishIngredient,
    models.DishReport,
    models.Ingredient,
    models.User,
    models.DishIngredientStandardPortion,
    models.CartItem,
    models.CartItemIngredient,
    models.Order,
    models.OrderDish,
    models.Recommendations
]

admin.site.register(api_models)
