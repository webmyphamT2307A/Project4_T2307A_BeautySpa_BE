const express = require('express');
const app = express();
const staffRoutes = require('./routes/staff');

app.use('/api/staffs', staffRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
