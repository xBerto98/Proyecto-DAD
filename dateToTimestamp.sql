ALTER TABLE `dad`.`buzzers` 
CHANGE COLUMN `tempBuzz` `tempBuzz` INT(10) NULL DEFAULT NULL ;
ALTER TABLE `dad`.`finalescarrera` 
CHANGE COLUMN `tempFC` `tempFC` INT(10) NULL DEFAULT NULL ;
ALTER TABLE `dad`.`sensorespir` 
CHANGE COLUMN `tempPir` `tempPir` INT(10) NULL DEFAULT NULL ;
ALTER TABLE `dad`.`servos` 
CHANGE COLUMN `tempServos` `tempServos` INT(10) NULL DEFAULT NULL ;
ALTER TABLE `dad`.`tecladosnumericos` 
CHANGE COLUMN `tempTN` `tempTN` INT(10) NULL DEFAULT NULL ;