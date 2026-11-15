package com.mvi.blueprint.data.database

import com.mvi.blueprint.data.database.entity.OrderEntity

object OrderDataSeeder {

    fun generateInitialOrders(): List<OrderEntity> {
        val now = System.currentTimeMillis()
        val oneHour = 3_600_000L

        return listOf(
            OrderEntity(
                id = "ORD-9481",
                customerName = "Apex Robotics",
                sku = "NV-H200-SXM",
                productName = "NVIDIA H200 Tensor Core GPU 141GB",
                itemCount = 8,
                totalAmount = 264_000.00,
                status = "PENDING",
                priority = "URGENT",
                createdAt = now - (1 * oneHour),
                destinationCity = "San Jose, CA"
            ),
            OrderEntity(
                id = "ORD-9482",
                customerName = "Starlight Systems",
                sku = "APL-MBP16-M4M",
                productName = "MacBook Pro 16\" M4 Max (128GB Unified, 4TB SSD)",
                itemCount = 12,
                totalAmount = 58_788.00,
                status = "PENDING",
                priority = "HIGH",
                createdAt = now - (2 * oneHour),
                destinationCity = "Seattle, WA"
            ),
            OrderEntity(
                id = "ORD-9483",
                customerName = "Nexus BioTech Labs",
                sku = "ILL-NOVA6000",
                productName = "NovaSeq 6000 Sequencing System Reagent Kit",
                itemCount = 4,
                totalAmount = 43_920.00,
                status = "APPROVED",
                priority = "HIGH",
                createdAt = now - (3 * oneHour),
                destinationCity = "Cambridge, MA"
            ),
            OrderEntity(
                id = "ORD-9484",
                customerName = "Quantum Financial",
                sku = "CSCO-CAT9300",
                productName = "Cisco Catalyst 9300 48-Port PoE+ Managed Switch",
                itemCount = 16,
                totalAmount = 67_200.00,
                status = "PROCESSING",
                priority = "NORMAL",
                createdAt = now - (5 * oneHour),
                destinationCity = "New York, NY"
            ),
            OrderEntity(
                id = "ORD-9485",
                customerName = "Hyperion Dynamics",
                sku = "GGL-PX9-FLD",
                productName = "Google Pixel 9 Pro Fold (512GB Obsidian Enterprise)",
                itemCount = 25,
                totalAmount = 49_975.00,
                status = "PENDING",
                priority = "NORMAL",
                createdAt = now - (6 * oneHour),
                destinationCity = "Austin, TX"
            ),
            OrderEntity(
                id = "ORD-9486",
                customerName = "CyberNet Defense",
                sku = "YUB-5NFC-100",
                productName = "YubiKey 5 NFC Hardware FIDO2 Enterprise 100-Pack",
                itemCount = 5,
                totalAmount = 27_500.00,
                status = "SHIPPED",
                priority = "HIGH",
                createdAt = now - (8 * oneHour),
                destinationCity = "Washington, DC"
            ),
            OrderEntity(
                id = "ORD-9487",
                customerName = "Solaris Media Studios",
                sku = "SNY-FX6-CINE",
                productName = "Sony FX6 Full-Frame Cinema Camera Kit with 24-70mm G-Master",
                itemCount = 3,
                totalAmount = 21_897.00,
                status = "PENDING",
                priority = "URGENT",
                createdAt = now - (10 * oneHour),
                destinationCity = "Los Angeles, CA"
            ),
            OrderEntity(
                id = "ORD-9488",
                customerName = "OmniCore Logistics",
                sku = "ZBR-TC57-SCN",
                productName = "Zebra TC57 Enterprise Rugged Mobile Scanner",
                itemCount = 40,
                totalAmount = 59_600.00,
                status = "DELIVERED",
                priority = "LOW",
                createdAt = now - (14 * oneHour),
                destinationCity = "Chicago, IL"
            ),
            OrderEntity(
                id = "ORD-9489",
                customerName = "Vanguard Aerospace",
                sku = "DELL-R760-XE",
                productName = "Dell PowerEdge R760 Rack Server Dual Xeon Platinum",
                itemCount = 6,
                totalAmount = 94_200.00,
                status = "PENDING",
                priority = "URGENT",
                createdAt = now - (16 * oneHour),
                destinationCity = "Huntsville, AL"
            ),
            OrderEntity(
                id = "ORD-9490",
                customerName = "Aegis Healthcare",
                sku = "BAR-MDNC-3421",
                productName = "Barco Nio Color 3MP Diagnostic Display Monitor",
                itemCount = 10,
                totalAmount = 34_500.00,
                status = "CANCELLED",
                priority = "LOW",
                createdAt = now - (20 * oneHour),
                destinationCity = "Rochester, MN"
            ),
            OrderEntity(
                id = "ORD-9491",
                customerName = "BlueHorizon Cloud",
                sku = "SNT-NVME-30T",
                productName = "Solidigm D5-P5336 30.72TB Enterprise NVMe U.2 SSD",
                itemCount = 20,
                totalAmount = 45_800.00,
                status = "PROCESSING",
                priority = "HIGH",
                createdAt = now - (24 * oneHour),
                destinationCity = "Dallas, TX"
            ),
            OrderEntity(
                id = "ORD-9492",
                customerName = "Titan Heavy Industries",
                sku = "PAN-FZ40-TOUGH",
                productName = "Panasonic Toughbook 40 Fully Rugged Laptop",
                itemCount = 15,
                totalAmount = 73_500.00,
                status = "PENDING",
                priority = "NORMAL",
                createdAt = now - (28 * oneHour),
                destinationCity = "Detroit, MI"
            ),
            OrderEntity(
                id = "ORD-9493",
                customerName = "Zephyr Telecom",
                sku = "JUN-MX204-CHAS",
                productName = "Juniper MX204 400G Compact Edge Router Chassis",
                itemCount = 2,
                totalAmount = 39_000.00,
                status = "APPROVED",
                priority = "NORMAL",
                createdAt = now - (32 * oneHour),
                destinationCity = "Denver, CO"
            ),
            OrderEntity(
                id = "ORD-9494",
                customerName = "Aurora Autonomous",
                sku = "OUST-OS1-128",
                productName = "Ouster OS1-128 Rev 7 High-Resolution LiDAR Sensor",
                itemCount = 6,
                totalAmount = 48_000.00,
                status = "SHIPPED",
                priority = "HIGH",
                createdAt = now - (36 * oneHour),
                destinationCity = "Pittsburgh, PA"
            ),
            OrderEntity(
                id = "ORD-9495",
                customerName = "Crestline Insurance",
                sku = "LEN-TP-P1G7",
                productName = "Lenovo ThinkPad P1 Gen 7 Intel Core Ultra 9 Workstation",
                itemCount = 30,
                totalAmount = 88_500.00,
                status = "PENDING",
                priority = "NORMAL",
                createdAt = now - (40 * oneHour),
                destinationCity = "Hartford, CT"
            )
        )
    }
}
