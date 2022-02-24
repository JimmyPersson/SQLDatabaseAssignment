
Delimiter **
create trigger update_stock
	before insert on orderedItems
	for each row
begin
	update shoes set shoes.stock = shoes.stock -1 where shoes.id = new.shoeId;
if (select stock from shoes where shoes.id = new.shoeId) = 0 then
	insert into orderpoint (shoeId) values (new.shoeId);
end if;
end**
Delimiter ;
	
	


Delimiter ** 
create procedure AddToCart(IN customerId int, IN orderSummaryId int, IN shoeId int, OUT newOrderId integer)
begin
	declare neww int default 0;

	declare exit handler for sqlexception
    begin
		rollback;
        select ('Exception occured, initiating rollback') as error;
	end;
	Start transaction;
	if (select stock from shoes where shoes.id = shoeId) > 0 then 
	
	if exists(select * from orderSummary where id = orderSummaryId) then
		if exists(select * from customer where id = customerId) then
		insert into orderedItems (orderSummaryId, shoeId) values (orderSummaryId, shoeId);
        else select ('No such customerId') as error;
        end if;
	end if;
	
	if not exists(select * from orderSummary where id = orderSummaryId) then
		if exists(select * from customer where id = customerId) then
			insert into orderSummary (customerId) values (customerId);
			select LAST_INSERT_ID() into neww;
			insert into orderedItems (orderSummaryId, shoeId) values (neww, shoeId);
            select neww into neworderid;
	end if;
    end if;
	else
		select ('Shoe is out of stock') as error;
	end if;
	Commit;
		
	
	end**
	Delimiter ;
	