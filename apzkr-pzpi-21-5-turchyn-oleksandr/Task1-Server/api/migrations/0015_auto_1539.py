# Generated by Django 3.1.3 on 2024-05-08 15:39

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0014_remove_order_cart_item'),
    ]

    operations = [
        migrations.CreateModel(
            name='OrderedDishIngredient',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('weight_or_volume', models.FloatField()),
                ('order_date', models.DateField()),
                ('order_time', models.TimeField()),
                ('ingredient', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.dishingredient')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.RemoveField(
            model_name='cartitemingredient',
            name='cart_item',
        ),
        migrations.RemoveField(
            model_name='cartitemingredient',
            name='ingredient',
        ),
        migrations.RemoveField(
            model_name='order',
            name='machine',
        ),
        migrations.DeleteModel(
            name='CartItem',
        ),
        migrations.DeleteModel(
            name='CartItemIngredient',
        ),
        migrations.DeleteModel(
            name='Order',
        ),
    ]
