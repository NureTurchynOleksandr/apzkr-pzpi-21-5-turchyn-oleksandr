# Generated by Django 3.1.3 on 2024-05-08 12:04

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0005_auto_1105'),
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
    ]
