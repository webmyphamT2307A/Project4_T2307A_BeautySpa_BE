const express = require('express');
const router = express.Router();


const staffs = [
    { id: 1, name: "Oliva Mia", position: "Spa & Beauty Expert", image: "link-ảnh-1" },
    { id: 2, name: "Charlotte Ross", position: "Spa & Beauty Expert", image: "link-ảnh-2" },
    { id: 3, name: "Amelia Luna", position: "Spa & Beauty Expert", image: "link-ảnh-3" },
    { id: 4, name: "Isabella Evelyn", position: "Spa & Beauty Expert", image: "link-ảnh-4" },
    { id: 5, name: "Luna Emma", position: "Spa & Beauty Expert", image: "link-ảnh-5" },
    { id: 6, name: "Mia Sophia", position: "Spa & Beauty Expert", image: "link-ảnh-6" },
    // Thêm tùy thích
];

router.get('/', (req, res) => {
    res.json(staffs);
});

module.exports = router;
