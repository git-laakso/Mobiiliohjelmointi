const express = require("express"),
app = express(),
port = 3000,
path = require("path");
app.use(express.urlencoded({ extended: true }));

let data = {
   "bonusPrize": "AB"
}

JSON.stringify(data)
console.log("Raffle code that wins is: ", data)

app.get("/redeem", (req, res) => {
   res.sendFile(path.join(__dirname, "public", "mainPage.html"));
});

app.get("/won", (req, res) => {
   res.sendFile(path.join(__dirname, "public", "won.html"));
});

app.get("/lost", (req, res) => {
   res.sendFile(path.join(__dirname, "public", "lost.html"));
});

app.post("/landing", (req, res) => {
   res.sendFile(path.join(__dirname, "public", "redirect.html"));
   console.log("User input from main page:", req.body.raffle)

   if (req.body.raffle == data.bonusPrize) {
      console.log("User won! " + req.body.raffle + " equals " + data.bonusPrize);
      res.redirect("/won")
   } else {
      console.log("Entered code does not match raffle code")
      res.redirect("/lost")
   }
});

app.get("/", (req, res) => {
   res.send(data);
});

app.listen(port, () => {
    console.log("App is running on port " + port);
});